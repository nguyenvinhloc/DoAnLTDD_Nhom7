package com.example.doannhom7.data.model

data class CustomerDebtRow(
    val customerId: Long,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val totalSales: Long,
    val totalPaid: Long,
    val debt: Long
)