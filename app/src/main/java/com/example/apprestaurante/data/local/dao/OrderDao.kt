package com.example.apprestaurante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.apprestaurante.data.local.entity.OrderEntity
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.model.OrderSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertItems(items: List<OrderItemEntity>)

    @Query("""
        SELECT o.id, o.userId, u.fullName AS clientName, u.email AS clientEmail,
               o.total, o.paymentMethod, o.paymentStatus, o.serviceType,
               o.tableNumber, o.deliveryAddress, o.documentType, o.notes,
               o.status, o.createdAt, COUNT(oi.id) AS itemCount
        FROM orders o
        INNER JOIN users u ON u.id = o.userId
        LEFT JOIN order_items oi ON oi.orderId = o.id
        WHERE o.userId = :userId
        GROUP BY o.id
        ORDER BY o.createdAt DESC
    """)
    fun observeForClient(userId: Long): Flow<List<OrderSummary>>

    @Query("""
        SELECT o.id, o.userId, u.fullName AS clientName, u.email AS clientEmail,
               o.total, o.paymentMethod, o.paymentStatus, o.serviceType,
               o.tableNumber, o.deliveryAddress, o.documentType, o.notes,
               o.status, o.createdAt, COUNT(oi.id) AS itemCount
        FROM orders o
        INNER JOIN users u ON u.id = o.userId
        LEFT JOIN order_items oi ON oi.orderId = o.id
        WHERE (:status = 'TODOS' OR o.status = :status)
        GROUP BY o.id
        ORDER BY CASE o.status
            WHEN 'PENDING' THEN 0
            WHEN 'CONFIRMED' THEN 1
            WHEN 'PREPARING' THEN 2
            WHEN 'READY' THEN 3
            WHEN 'DELIVERED' THEN 4
            ELSE 5 END,
            o.createdAt DESC
    """)
    fun observeAll(status: String): Flow<List<OrderSummary>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId AND userId = :userId LIMIT 1")
    suspend fun getForClient(orderId: Long, userId: Long): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id")
    suspend fun getItems(orderId: Long): List<OrderItemEntity>

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: String, updatedAt: Long): Int

    @Query("UPDATE orders SET paymentStatus = 'PAID', paidAt = :paidAt, updatedAt = :paidAt WHERE id = :orderId")
    suspend fun markPaid(orderId: Long, paidAt: Long): Int

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun delete(orderId: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE status IN ('PENDING','CONFIRMED','PREPARING','READY')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM orders WHERE status = 'DELIVERED' AND paymentStatus = 'PAID'")
    fun observeDeliveredSales(): Flow<Double>
}
