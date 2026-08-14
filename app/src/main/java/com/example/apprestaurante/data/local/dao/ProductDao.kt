package com.example.apprestaurante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apprestaurante.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("""
        SELECT * FROM products
        WHERE (:includeInactive = 1 OR isActive = 1)
          AND (:category = 'Todos' OR category = :category)
          AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY isActive DESC, name COLLATE NOCASE
    """)
    fun observeFiltered(
        query: String,
        category: String,
        includeInactive: Boolean
    ): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products WHERE isActive = 1 ORDER BY category")
    fun observeCategories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getById(productId: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity): Int

    @Query("UPDATE products SET isActive = :active, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun setActive(
        productId: Long,
        active: Boolean,
        updatedAt: Long
    ): Int

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stock <= 5")
    fun observeLowStockCount(): Flow<Int>

    @Query("""
        UPDATE products
        SET stock = stock - :quantity, updatedAt = :updatedAt
        WHERE id = :productId AND isActive = 1 AND stock >= :quantity
    """)
    suspend fun decreaseStock(
        productId: Long,
        quantity: Int,
        updatedAt: Long
    ): Int

    @Query("""
        UPDATE products
        SET stock = stock + :quantity, updatedAt = :updatedAt
        WHERE id = :productId
    """)
    suspend fun increaseStock(
        productId: Long,
        quantity: Int,
        updatedAt: Long
    ): Int
}
