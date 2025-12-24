package com.example.doannhom7.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inbounds",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("createdByUserId"), Index("createdAt")]
)
data class InboundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val createdByUserId: Long,
    val note: String? = null,
    val totalAmount: Long = 0
)