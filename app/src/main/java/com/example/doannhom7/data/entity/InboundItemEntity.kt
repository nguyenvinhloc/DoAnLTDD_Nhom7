package com.example.doannhom7.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inbound_items",
    foreignKeys = [
        ForeignKey(
            entity = InboundEntity::class,
            parentColumns = ["id"],
            childColumns = ["inboundId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("inboundId"), Index("productId")]
)
data class InboundItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inboundId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val quantity: Int,
    val unitCost: Long,           // giá nhập
    val lineTotal: Long
)