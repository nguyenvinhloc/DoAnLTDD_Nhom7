package com.example.doannhom7.data.entity

import androidx.room.*

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["customerId"]), Index(value = ["type"]), Index(value = ["status"])]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: InvoiceType,                 // IN / OUT / MERGE
    val status: InvoiceStatus = InvoiceStatus.OPEN,
    val customerId: Long? = null,          // OUT/MERGE cần customer
    val affectsStock: Int = 1,             // 1: có trừ/tăng kho, 0: không (MERGE)
    val totalAmount: Long = 0,
    val note: String? = null,
    val mergedToId: Long? = null,          // nếu invoice này đã bị gộp vào invoice khác
    val createdAt: Long = System.currentTimeMillis()
)
