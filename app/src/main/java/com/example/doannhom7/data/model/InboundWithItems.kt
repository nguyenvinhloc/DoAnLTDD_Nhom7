package com.example.doannhom7.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.doannhom7.data.entity.InboundEntity
import com.example.doannhom7.data.entity.InboundItemEntity

data class InboundWithItems(
    @Embedded val inbound: InboundEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "inboundId"
    )
    val items: List<InboundItemEntity>
)