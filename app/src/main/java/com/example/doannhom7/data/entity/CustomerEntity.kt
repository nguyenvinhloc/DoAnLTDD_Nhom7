package com.example.doannhom7.data.entity

import androidx.room.*

@Entity(
    tableName = "customers",
    indices = [Index(value = ["phone"], unique = true)]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
