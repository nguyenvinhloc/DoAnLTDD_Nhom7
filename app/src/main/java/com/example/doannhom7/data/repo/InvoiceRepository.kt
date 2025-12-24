package com.example.doannhom7.data.repo

import androidx.room.withTransaction
import com.example.doannhom7.data.db.AppDatabase
import com.example.doannhom7.data.entity.*

data class DraftLine(
    val productId: Long,
    val name: String,
    val unit: String,
    val qty: Int,
    val unitPrice: Long
) {
    val lineTotal: Long = qty.toLong() * unitPrice
}

class InvoiceRepository(private val db: AppDatabase) {

    suspend fun createInInvoice(lines: List<DraftLine>, note: String? = null): Long {
        require(lines.isNotEmpty())
        return db.withTransaction {
            val total = lines.sumOf { it.lineTotal }
            val invoiceId = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    type = InvoiceType.IN,
                    status = InvoiceStatus.OPEN,
                    customerId = null,
                    affectsStock = 1,
                    totalAmount = total,
                    note = note
                )
            )

            val items = lines.map {
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productId = it.productId,
                    productName = it.name,
                    unit = it.unit,
                    qty = it.qty,
                    unitPrice = it.unitPrice,
                    lineTotal = it.lineTotal
                )
            }
            db.invoiceDao().insertItems(items)

            lines.forEach { line -> db.productDao().increaseStock(line.productId, line.qty) }
            invoiceId
        }
    }

    suspend fun createOutInvoice(customerId: Long, lines: List<DraftLine>, note: String? = null): Long {
        require(lines.isNotEmpty())
        return db.withTransaction {

            // check tồn
            lines.forEach { line ->
                val ok = db.productDao().hasEnoughStock(line.productId, line.qty)
                if (!ok) throw IllegalStateException("Không đủ tồn kho cho: ${line.name}")
            }

            val total = lines.sumOf { it.lineTotal }
            val invoiceId = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    type = InvoiceType.OUT,
                    status = InvoiceStatus.OPEN,
                    customerId = customerId,
                    affectsStock = 1,
                    totalAmount = total,
                    note = note
                )
            )

            val items = lines.map {
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productId = it.productId,
                    productName = it.name,
                    unit = it.unit,
                    qty = it.qty,
                    unitPrice = it.unitPrice,
                    lineTotal = it.lineTotal
                )
            }
            db.invoiceDao().insertItems(items)

            // trừ tồn
            lines.forEach { line -> db.productDao().decreaseStock(line.productId, line.qty) }

            invoiceId
        }
    }

    suspend fun mergeOutInvoices(customerId: Long, invoiceIds: List<Long>): Long {
        require(invoiceIds.size >= 2)
        return db.withTransaction {

            val details = invoiceIds.mapNotNull { db.invoiceDao().getInvoiceWithItems(it) }
            if (details.size != invoiceIds.size) throw IllegalStateException("Thiếu dữ liệu phiếu")

            // ✅ chỉ cho gộp phiếu OUT đang OPEN và cùng customer
            details.forEach { d ->
                if (d.invoice.type != InvoiceType.OUT) throw IllegalStateException("Chỉ gộp phiếu OUT")
                if (d.invoice.status != InvoiceStatus.OPEN) throw IllegalStateException("Chỉ gộp phiếu OPEN")
                if (d.invoice.customerId != customerId) throw IllegalStateException("Khác khách hàng")
                if (d.invoice.mergedToId != null) throw IllegalStateException("Phiếu đã gộp trước đó")
            }

            // gom item theo productId (giữ unitPrice của lần đầu)
            val map = linkedMapOf<Long, DraftLine>()
            details.flatMap { it.items }.forEach { item ->
                val old = map[item.productId]
                if (old == null) {
                    map[item.productId] = DraftLine(item.productId, item.productName, item.unit, item.qty, item.unitPrice)
                } else {
                    map[item.productId] = old.copy(qty = old.qty + item.qty)
                }
            }

            val lines = map.values.toList()
            val total = lines.sumOf { it.lineTotal }

            // ✅ tạo phiếu MERGE (không trừ kho)
            val mergeId = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    type = InvoiceType.MERGE,
                    status = InvoiceStatus.OPEN,
                    customerId = customerId,
                    affectsStock = 0,
                    totalAmount = total,
                    note = "Gộp ${invoiceIds.size} phiếu",
                    mergedToId = null
                )
            )

            val items = lines.map {
                InvoiceItemEntity(
                    invoiceId = mergeId,
                    productId = it.productId,
                    productName = it.name,
                    unit = it.unit,
                    qty = it.qty,
                    unitPrice = it.unitPrice,
                    lineTotal = it.lineTotal
                )
            }
            db.invoiceDao().insertItems(items)

            // ✅ đánh dấu phiếu cũ đã bị gộp
            db.invoiceDao().markMerged(invoiceIds, mergeId)

            mergeId
        }
    }

    // ===== NEW: XÓA phiếu OUT (hoàn kho) =====
    suspend fun deleteOutInvoice(invoiceId: Long) {
        db.withTransaction {
            val detail = db.invoiceDao().getInvoiceWithItems(invoiceId)
                ?: throw IllegalStateException("Không tìm thấy phiếu")

            val inv = detail.invoice
            if (inv.type != InvoiceType.OUT) throw IllegalStateException("Không phải phiếu xuất")
            if (inv.mergedToId != null) throw IllegalStateException("Phiếu đã gộp, không được xóa")
            if (inv.status != InvoiceStatus.OPEN) throw IllegalStateException("Chỉ xóa phiếu OPEN")

            if (inv.affectsStock == 1) {
                detail.items.forEach { item ->
                    db.productDao().increaseStock(item.productId, item.qty)
                }
            }

            db.invoiceDao().deleteInvoiceById(invoiceId)
        }
    }

    // ===== NEW: SỬA phiếu OUT (hoàn kho cũ -> check tồn -> trừ kho mới) =====
    suspend fun updateOutInvoice(
        invoiceId: Long,
        newCustomerId: Long,
        newLines: List<DraftLine>,
        note: String? = null
    ) {
        require(newLines.isNotEmpty())
        db.withTransaction {
            val old = db.invoiceDao().getInvoiceWithItems(invoiceId)
                ?: throw IllegalStateException("Không tìm thấy phiếu")

            val inv = old.invoice
            if (inv.type != InvoiceType.OUT) throw IllegalStateException("Không phải phiếu xuất")
            if (inv.mergedToId != null) throw IllegalStateException("Phiếu đã gộp, không được sửa")
            if (inv.status != InvoiceStatus.OPEN) throw IllegalStateException("Chỉ sửa phiếu OPEN")

            // hoàn kho theo items cũ
            if (inv.affectsStock == 1) {
                old.items.forEach { db.productDao().increaseStock(it.productId, it.qty) }
            }

            // check tồn theo items mới
            newLines.forEach {
                val ok = db.productDao().hasEnoughStock(it.productId, it.qty)
                if (!ok) throw IllegalStateException("Không đủ tồn cho: ${it.name}")
            }

            // trừ kho theo items mới
            if (inv.affectsStock == 1) {
                newLines.forEach { db.productDao().decreaseStock(it.productId, it.qty) }
            }

            val total = newLines.sumOf { it.lineTotal }
            db.invoiceDao().updateOutInvoiceHeader(invoiceId, newCustomerId, total, note)

            db.invoiceDao().deleteItemsOfInvoice(invoiceId)
            val items = newLines.map {
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productId = it.productId,
                    productName = it.name,
                    unit = it.unit,
                    qty = it.qty,
                    unitPrice = it.unitPrice,
                    lineTotal = it.lineTotal
                )
            }
            db.invoiceDao().insertItems(items)
        }
    }
}
