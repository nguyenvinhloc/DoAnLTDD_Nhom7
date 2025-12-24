package com.example.doannhom7.data.dao

import androidx.room.*
import com.example.doannhom7.data.entity.CustomerEntity

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(c: CustomerEntity): Long

    @Update
    suspend fun update(c: CustomerEntity)

    @Delete
    suspend fun delete(c: CustomerEntity)

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    suspend fun getAll(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id=:id LIMIT 1")
    suspend fun getById(id: Long): CustomerEntity?

    @Query("""
        SELECT * FROM customers
        WHERE shopName LIKE :q OR ownerName LIKE :q OR phone LIKE :q OR address LIKE :q
        ORDER BY shopName ASC
    """)
    suspend fun search(q: String): List<CustomerEntity>

    // ✅ THÊM CHO STATS
    @Query("SELECT COUNT(*) FROM customers")
    suspend fun count(): Int
}
