package com.example.doannhom7.data.entity

import androidx.room.*

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["code"], unique = true)
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val code: String,      // mã hàng / SKU
    val unit: String,      // cái, mét, cuộn...
    val price: Long,       // giá bán tham khảo
    val stockQty: Int = 0, // tồn kho hiện tại (đơn giản hóa)
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
