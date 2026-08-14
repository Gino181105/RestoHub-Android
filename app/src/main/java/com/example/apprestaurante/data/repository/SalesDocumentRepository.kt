package com.example.apprestaurante.data.repository

import com.example.apprestaurante.data.local.RestoHubDatabase

class SalesDocumentRepository(
    private val db: RestoHubDatabase
) {
    private val dao = db.salesDocumentDao()

    fun observeDocuments(query: String, type: String) = dao.observeAll(query.trim(), type)
    fun observeForClient(userId: Long) = dao.observeForClient(userId)
    fun observeCount() = dao.observeCount()
    fun observeTotalSales() = dao.observeTotalSales()

    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun findByOrder(orderId: Long) = dao.findByOrder(orderId)
    suspend fun getOrder(orderId: Long) = db.orderDao().getById(orderId)
    suspend fun getItems(orderId: Long) = db.orderDao().getItems(orderId)
}
