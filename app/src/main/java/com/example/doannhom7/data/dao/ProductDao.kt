package com.example.doannhom7.data.dao

import androidx.room.*
import com.example.doannhom7.data.entity.ProductEntity

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(p: ProductEntity): Long

    @Update
    suspend fun update(p: ProductEntity)

    @Delete
    suspend fun delete(p: ProductEntity)

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id=:id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("""
        SELECT * FROM products
        WHERE name LIKE :q OR code LIKE :q OR unit LIKE :q
        ORDER BY name ASC
    """)
    suspend fun search(q: String): List<ProductEntity>

    // ===== tồn kho (user nhập/xuất dùng) =====
    @Query("UPDATE products SET stockQty = stockQty + :qty WHERE id=:productId")
    suspend fun increaseStock(productId: Long, qty: Int)

    @Query("UPDATE products SET stockQty = stockQty - :qty WHERE id=:productId AND stockQty >= :qty")
    suspend fun decreaseStock(productId: Long, qty: Int)

    @Query("SELECT CASE WHEN stockQty >= :qty THEN 1 ELSE 0 END FROM products WHERE id=:productId")
    suspend fun hasEnoughStock(productId: Long, qty: Int): Boolean

    // ✅ THÊM CHO STATS
    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Query("SELECT COALESCE(SUM(stockQty),0) FROM products")
    suspend fun sumStock(): Int

    // tổng giá trị tồn = SUM(stockQty * price)
    @Query("SELECT COALESCE(SUM(stockQty * price),0) FROM products")
    suspend fun sumStockValue(): Long
}
