package com.example.doannhom7.data.dao

import androidx.room.*
import com.example.doannhom7.data.entity.*

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItemEntity>
)

data class OutInvoiceWithCustomer(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: CustomerEntity?
)

@Dao
interface InvoiceDao {

    @Insert suspend fun insertInvoice(i: InvoiceEntity): Long
    @Insert suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM invoices WHERE id=:id LIMIT 1")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id=:id LIMIT 1")
    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems?

    // OPEN cần thu tiền của khách: OUT hoặc MERGE, loại bỏ phiếu đã bị gộp
    @Query("""
        SELECT * FROM invoices
        WHERE type IN ('OUT','MERGE')
          AND status='OPEN'
          AND customerId=:customerId
          AND mergedToId IS NULL
        ORDER BY createdAt DESC
    """)
    suspend fun getOpenOutInvoicesByCustomer(customerId: Long): List<InvoiceEntity>

    // Danh sách phiếu xuất: OUT hoặc MERGE
    @Query("""
        SELECT * FROM invoices
        WHERE type IN ('OUT','MERGE')
          AND mergedToId IS NULL
        ORDER BY createdAt DESC
    """)
    suspend fun getAllOutInvoices(): List<InvoiceEntity>

    // List phiếu xuất kèm customer: OUT hoặc MERGE
    @Transaction
    @Query("""
        SELECT * FROM invoices
        WHERE type IN ('OUT','MERGE')
          AND mergedToId IS NULL
        ORDER BY createdAt DESC
    """)
    suspend fun getOutInvoicesWithCustomer(): List<OutInvoiceWithCustomer>

    @Query("UPDATE invoices SET status=:status WHERE id=:invoiceId")
    suspend fun updateStatus(invoiceId: Long, status: InvoiceStatus)

    // phiếu OUT cũ -> MERGED + mergedToId = mergeId
    @Query("UPDATE invoices SET status='MERGED', mergedToId=:toId WHERE id IN (:ids)")
    suspend fun markMerged(ids: List<Long>, toId: Long)

    // Thanh toán theo khách: PAID cho cả OUT và MERGE OPEN (chưa bị gộp)
    @Query("""
        UPDATE invoices SET status='PAID'
        WHERE customerId=:customerId
          AND type IN ('OUT','MERGE')
          AND status='OPEN'
          AND mergedToId IS NULL
    """)
    suspend fun markOutPaidByCustomer(customerId: Long)

    // Nếu bạn dùng sửa/xóa
    @Query("DELETE FROM invoice_items WHERE invoiceId=:invoiceId")
    suspend fun deleteItemsOfInvoice(invoiceId: Long)

    @Query("DELETE FROM invoices WHERE id=:invoiceId")
    suspend fun deleteInvoiceById(invoiceId: Long)

    @Query("""
        UPDATE invoices
        SET customerId=:customerId, totalAmount=:total, note=:note
        WHERE id=:invoiceId
    """)
    suspend fun updateOutInvoiceHeader(invoiceId: Long, customerId: Long, total: Long, note: String?)
    @Query("""
    SELECT COALESCE(SUM(totalAmount),0)
    FROM invoices
    WHERE customerId=:customerId
      AND status='OPEN'
      AND type IN ('OUT','MERGE')
      AND mergedToId IS NULL
""")
    suspend fun getOpenDebtTotalByCustomer(customerId: Long): Long
}
