package com.example.doannhom7.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.doannhom7.data.entity.OutboundEntity
import com.example.doannhom7.data.entity.OutboundItemEntity

data class OutboundWithItems(
    @Embedded val outbound: OutboundEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "outboundId"
    )
    val items: List<OutboundItemEntity>
)