package com.example.doannhom7.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbounds",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("customerId"), Index("createdByUserId"), Index("status"), Index("createdAt")]
)
data class OutboundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val createdByUserId: Long,
    val note: String? = null,
    val status: OutboundStatus = OutboundStatus.PENDING,
    val totalAmount: Long = 0
)