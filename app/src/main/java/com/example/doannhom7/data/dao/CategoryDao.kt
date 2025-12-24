package com.example.doannhom7.data.dao

import androidx.room.*
import com.example.doannhom7.data.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(c: CategoryEntity): Long

    @Update
    suspend fun update(c: CategoryEntity)

    @Delete
    suspend fun delete(c: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY createdAt DESC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id=:id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    // ✅ THỐNG KÊ
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    // ✅ TÌM KIẾM (fragment gọi search(keyword) sẽ chạy đúng)
    @Query("""
        SELECT * FROM categories
        WHERE name LIKE '%' || :keyword || '%'
        ORDER BY name ASC
    """)
    suspend fun search(keyword: String): List<CategoryEntity>
}
