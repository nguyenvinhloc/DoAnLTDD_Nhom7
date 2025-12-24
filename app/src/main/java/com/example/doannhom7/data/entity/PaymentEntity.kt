package com.example.doannhom7.data.entity

import androidx.room.*

@Entity(
    tableName = "payments",
    indices = [Index(value = ["customerId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
