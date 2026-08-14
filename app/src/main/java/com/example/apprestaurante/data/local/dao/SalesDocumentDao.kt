package com.example.apprestaurante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDocumentDao {
    @Insert
    suspend fun insert(document: SalesDocumentEntity): Long

    @Query("SELECT * FROM sales_documents WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SalesDocumentEntity?

    @Query("SELECT * FROM sales_documents WHERE orderId = :orderId LIMIT 1")
    suspend fun findByOrder(orderId: Long): SalesDocumentEntity?

    @Query("""
        SELECT * FROM sales_documents
        WHERE (:type = 'TODOS' OR documentType = :type)
          AND (series || '-' || printf('%08d', number) LIKE '%' || :query || '%'
               OR clientName LIKE '%' || :query || '%'
               OR clientDocument LIKE '%' || :query || '%'
               OR businessName LIKE '%' || :query || '%')
        ORDER BY issuedAt DESC
    """)
    fun observeAll(query: String, type: String): Flow<List<SalesDocumentEntity>>

    @Query("SELECT COALESCE(MAX(number), 0) + 1 FROM sales_documents WHERE series = :series")
    suspend fun nextNumber(series: String): Int


    @Query("""
        SELECT d.* FROM sales_documents d
        INNER JOIN orders o ON o.id = d.orderId
        WHERE o.userId = :userId
        ORDER BY d.issuedAt DESC
    """)
    fun observeForClient(userId: Long): Flow<List<SalesDocumentEntity>>

    @Query("SELECT COUNT(*) FROM sales_documents")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales_documents")
    fun observeTotalSales(): Flow<Double>
}
