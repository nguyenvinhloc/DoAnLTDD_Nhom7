package com.example.doannhom7.data.dao

import androidx.room.*
import com.example.doannhom7.data.entity.InboundEntity
import com.example.doannhom7.data.entity.InboundItemEntity
import com.example.doannhom7.data.model.InboundWithItems

@Dao
interface InboundDao {

    @Query("SELECT COUNT(*) FROM inbounds")
    suspend fun count(): Int

    @Insert
    suspend fun insertInbound(i: InboundEntity): Long

    @Insert
    suspend fun insertItems(items: List<InboundItemEntity>)

    @Transaction
    suspend fun createInbound(
        createdByUserId: Long,
        note: String?,
        items: List<Triple<Long, Int, Long>>, // productId, qty, unitCost
        productNameResolver: suspend (Long) -> String
    ): Long {
        if (items.isEmpty()) throw IllegalArgumentException("Items empty")

        var total = 0L
        val inboundId = insertInbound(
            InboundEntity(createdByUserId = createdByUserId, note = note, totalAmount = 0)
        )

        val rows = items.map { (productId, qty, unitCost) ->
            val nameSnap = productNameResolver(productId)
            val line = unitCost * qty.toLong()
            total += line
            InboundItemEntity(
                inboundId = inboundId,
                productId = productId,
                productNameSnapshot = nameSnap,
                quantity = qty,
                unitCost = unitCost,
                lineTotal = line
            )
        }

        insertItems(rows)
        updateTotal(inboundId, total)
        return inboundId
    }

    @Query("UPDATE inbounds SET totalAmount = :total WHERE id = :id")
    suspend fun updateTotal(id: Long, total: Long)

    @Transaction
    @Query("SELECT * FROM inbounds ORDER BY createdAt DESC")
    suspend fun getAllWithItems(): List<InboundWithItems>

    @Transaction
    @Query("SELECT * FROM inbounds WHERE id = :id LIMIT 1")
    suspend fun getByIdWithItems(id: Long): InboundWithItems?
}