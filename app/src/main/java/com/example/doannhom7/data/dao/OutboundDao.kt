package com.example.doannhom7.data.dao

import androidx.room.*
import com.example.doannhom7.data.entity.OutboundEntity
import com.example.doannhom7.data.entity.OutboundItemEntity
import com.example.doannhom7.data.entity.OutboundStatus
import com.example.doannhom7.data.model.OutboundWithItems

@Dao
interface OutboundDao {

    @Query("SELECT COUNT(*) FROM outbounds")
    suspend fun count(): Int

    @Insert
    suspend fun insertOutbound(o: OutboundEntity): Long

    @Insert
    suspend fun insertItems(items: List<OutboundItemEntity>)

    @Query("UPDATE outbounds SET totalAmount = :total WHERE id = :id")
    suspend fun updateTotal(id: Long, total: Long)

    @Query("UPDATE outbounds SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: OutboundStatus)

    @Query("UPDATE outbounds SET status = :status, note = :note WHERE id IN (:ids)")
    suspend fun updateStatusForIds(ids: List<Long>, status: OutboundStatus, note: String?)

    @Transaction
    suspend fun createOutbound(
        customerId: Long,
        createdByUserId: Long,
        note: String?,
        items: List<Triple<Long, Int, Long>>, // productId, qty, unitPrice
        productNameResolver: suspend (Long) -> String
    ): Long {
        if (items.isEmpty()) throw IllegalArgumentException("Items empty")

        var total = 0L
        val outboundId = insertOutbound(
            OutboundEntity(
                customerId = customerId,
                createdByUserId = createdByUserId,
                note = note,
                status = OutboundStatus.PENDING,
                totalAmount = 0
            )
        )

        val rows = items.map { (productId, qty, unitPrice) ->
            val nameSnap = productNameResolver(productId)
            val line = unitPrice * qty.toLong()
            total += line
            OutboundItemEntity(
                outboundId = outboundId,
                productId = productId,
                productNameSnapshot = nameSnap,
                quantity = qty,
                unitPrice = unitPrice,
                lineTotal = line
            )
        }

        insertItems(rows)
        updateTotal(outboundId, total)
        return outboundId
    }

    @Transaction
    @Query("SELECT * FROM outbounds WHERE id = :id LIMIT 1")
    suspend fun getByIdWithItems(id: Long): OutboundWithItems?

    @Transaction
    @Query("SELECT * FROM outbounds ORDER BY createdAt DESC")
    suspend fun getAllWithItems(): List<OutboundWithItems>

    @Transaction
    @Query("SELECT * FROM outbounds WHERE customerId = :customerId ORDER BY createdAt DESC")
    suspend fun getByCustomerWithItems(customerId: Long): List<OutboundWithItems>

    @Query("""
        SELECT * FROM outbounds 
        WHERE customerId = :customerId AND status = 'PENDING'
        ORDER BY createdAt DESC
    """)
    suspend fun getPendingForCustomer(customerId: Long): List<OutboundEntity>

    @Transaction
    @Query("SELECT * FROM outbounds WHERE id IN (:ids)")
    suspend fun getOutboundsWithItemsByIds(ids: List<Long>): List<OutboundWithItems>

    //GỘP ĐƠN: gộp nhiều outbounds PENDING của 1 khách -> tạo 1 outbound mới (PENDING)
    @Transaction
    suspend fun mergeOutbounds(
        customerId: Long,
        createdByUserId: Long,
        outboundIds: List<Long>,
        note: String?,
        productNameResolver: suspend (Long) -> String
    ): Long {
        if (outboundIds.size < 2) throw IllegalArgumentException("Need >= 2 orders to merge")

        val details = getOutboundsWithItemsByIds(outboundIds)

        // validate cùng khách + PENDING
        details.forEach {
            if (it.outbound.customerId != customerId) throw IllegalArgumentException("Different customer")
            if (it.outbound.status != OutboundStatus.PENDING) throw IllegalArgumentException("Only PENDING can merge")
        }

        // gộp items theo productId
        val mapQty = linkedMapOf<Long, Pair<Int, Long>>() // productId -> (qtySum, unitPrice)
        details.flatMap { it.items }.forEach { item ->
            val old = mapQty[item.productId]
            val newQty = (old?.first ?: 0) + item.quantity
            val price = old?.second ?: item.unitPrice
            mapQty[item.productId] = Pair(newQty, price)
        }

        val newItemsTriples = mapQty.map { (productId, pair) ->
            Triple(productId, pair.first, pair.second)
        }

        val newId = createOutbound(
            customerId = customerId,
            createdByUserId = createdByUserId,
            note = note ?: "Gộp từ các đơn: ${outboundIds.joinToString()}",
            items = newItemsTriples,
            productNameResolver = productNameResolver
        )

        // hủy đơn cũ
        updateStatusForIds(
            ids = outboundIds,
            status = OutboundStatus.CANCELED,
            note = "Đã gộp vào đơn #$newId"
        )

        return newId
    }

    // Tính doanh số theo khách (dùng cho công nợ): chỉ tính DONE/CONFIRMED/DELIVERING tùy bạn
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0) 
        FROM outbounds 
        WHERE customerId = :customerId AND status IN ('CONFIRMED','DONE')
    """)
    suspend fun sumSalesForCustomer(customerId: Long): Long
}