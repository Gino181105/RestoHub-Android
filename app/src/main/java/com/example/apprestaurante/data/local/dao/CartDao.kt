package com.example.apprestaurante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.apprestaurante.data.local.entity.CartItemEntity
import com.example.apprestaurante.data.local.model.CartProductItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("""
        SELECT c.id AS cartId, p.id AS productId, p.name, p.imageUri,
               p.price, p.stock, c.quantity, (p.price * c.quantity) AS subtotal
        FROM cart_items c
        INNER JOIN products p ON p.id = c.productId
        WHERE c.userId = :userId
        ORDER BY c.createdAt DESC
    """)
    fun observeCart(userId: Long): Flow<List<CartProductItem>>

    @Query("""
        SELECT c.id AS cartId, p.id AS productId, p.name, p.imageUri,
               p.price, p.stock, c.quantity, (p.price * c.quantity) AS subtotal
        FROM cart_items c
        INNER JOIN products p ON p.id = c.productId
        WHERE c.userId = :userId
        ORDER BY c.createdAt DESC
    """)
    suspend fun getCart(userId: Long): List<CartProductItem>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun findItem(userId: Long, productId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: CartItemEntity): Long

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :cartId")
    suspend fun updateQuantity(cartId: Long, quantity: Int): Int

    @Query("DELETE FROM cart_items WHERE id = :cartId")
    suspend fun delete(cartId: Long): Int

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clear(userId: Long): Int

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE userId = :userId")
    fun observeItemCount(userId: Long): Flow<Int>
}
