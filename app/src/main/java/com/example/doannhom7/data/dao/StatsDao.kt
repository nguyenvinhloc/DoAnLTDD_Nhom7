package com.example.doannhom7.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.doannhom7.data.model.CustomerDebtRow

@Dao
interface StatsDao {

    // Tổng nhập theo khoảng thời gian
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0) 
        FROM inbounds 
        WHERE createdAt BETWEEN :from AND :to
    """)
    suspend fun sumInbound(from: Long, to: Long): Long

    // Tổng xuất theo khoảng thời gian (đếm CONFIRMED/DONE)
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0) 
        FROM outbounds 
        WHERE createdAt BETWEEN :from AND :to 
          AND status IN ('CONFIRMED','DONE')
    """)
    suspend fun sumOutbound(from: Long, to: Long): Long

    // Top khách nợ nhiều
    @Query("""
        SELECT 
            c.id AS customerId,
            c.shopName AS shopName,
            c.ownerName AS ownerName,
            c.phone AS phone,
            COALESCE((
                SELECT SUM(o.totalAmount)
                FROM outbounds o
                WHERE o.customerId = c.id AND o.status IN ('CONFIRMED','DONE')
            ), 0) AS totalSales,
            COALESCE((
                SELECT SUM(p.amount)
                FROM payments p
                WHERE p.customerId = c.id
            ), 0) AS totalPaid,
            (
                COALESCE((
                    SELECT SUM(o.totalAmount)
                    FROM outbounds o
                    WHERE o.customerId = c.id AND o.status IN ('CONFIRMED','DONE')
                ), 0) 
                - 
                COALESCE((
                    SELECT SUM(p.amount)
                    FROM payments p
                    WHERE p.customerId = c.id
                ), 0)
            ) AS debt
        FROM customers c
        ORDER BY debt DESC
        LIMIT :limit
    """)
    suspend fun topDebtCustomers(limit: Int = 10): List<CustomerDebtRow>
}