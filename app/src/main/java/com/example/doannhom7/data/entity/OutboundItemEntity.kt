package com.example.doannhom7.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbound_items",
    foreignKeys = [
        ForeignKey(
            entity = OutboundEntity::class,
            parentColumns = ["id"],
            childColumns = ["outboundId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("outboundId"), Index("productId")]
)
data class OutboundItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val outboundId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val quantity: Int,
    val unitPrice: Long,          // giá bán snapshot (thường lấy Product.salePrice)
    val lineTotal: Long
)