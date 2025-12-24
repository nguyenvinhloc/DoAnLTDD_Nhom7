package com.example.doannhom7.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.doannhom7.data.entity.PaymentEntity

@Dao
interface PaymentDao {

    @Insert
    suspend fun insert(p: PaymentEntity): Long

    @Query("""
        SELECT COALESCE(SUM(amount),0)
        FROM payments
        WHERE customerId=:customerId
    """)
    suspend fun getPaidTotalByCustomer(customerId: Long): Long

    @Query("""
        SELECT * FROM payments
        WHERE customerId=:customerId
        ORDER BY createdAt DESC
    """)
    suspend fun getPaymentsByCustomer(customerId: Long): List<PaymentEntity>
}
