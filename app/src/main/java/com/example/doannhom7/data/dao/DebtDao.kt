package com.example.doannhom7.data.dao

import androidx.room.Dao
import androidx.room.Query

data class CustomerDebtRow(
    val customerId: Long,
    val shopName: String,
    val phone: String,
    val totalOut: Long,     // tổng mua (OUT+MERGE, chưa bị gộp)
    val totalPaid: Long,    // tổng đã trả (payments)
    val debt: Long          // nợ = OPEN - totalPaid (không âm)
)

@Dao
interface DebtDao {

    @Query("""
        SELECT
            c.id AS customerId,
            c.shopName AS shopName,
            c.phone AS phone,

            -- Tổng mua (tất cả OUT/MERGE chưa bị gộp)
            COALESCE(SUM(CASE
                WHEN i.type IN ('OUT','MERGE') AND i.mergedToId IS NULL
                THEN i.totalAmount ELSE 0 END), 0) AS totalOut,

            -- Tổng đã trả (lấy từ payments)
            COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customerId = c.id), 0) AS totalPaid,

            -- Công nợ = Tổng OPEN (OUT/MERGE) - totalPaid, không âm
            MAX(
                COALESCE(SUM(CASE
                    WHEN i.type IN ('OUT','MERGE')
                     AND i.status='OPEN'
                     AND i.mergedToId IS NULL
                    THEN i.totalAmount ELSE 0 END), 0)
                -
                COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customerId = c.id), 0),
            0) AS debt

        FROM customers c
        LEFT JOIN invoices i ON i.customerId = c.id
        GROUP BY c.id, c.shopName, c.phone
        ORDER BY debt DESC, c.shopName ASC
    """)
    suspend fun getCustomerDebts(): List<CustomerDebtRow>
}
