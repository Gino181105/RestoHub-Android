package com.example.apprestaurante.data.repository

import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.dao.ProductDao
import com.example.apprestaurante.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    fun observeProducts(
        query: String,
        category: String,
        includeInactive: Boolean = false
    ): Flow<List<ProductEntity>> = dao.observeFiltered(query, category, includeInactive)

    fun observeCategories(): Flow<List<String>> = dao.observeCategories()
    fun observeActiveCount(): Flow<Int> = dao.observeActiveCount()
    fun observeLowStockCount(): Flow<Int> = dao.observeLowStockCount()
    suspend fun getById(id: Long): ProductEntity? = dao.getById(id)

    suspend fun save(product: ProductEntity): AppResult<Long> = runCatching {
        if (product.id == 0L) {
            dao.insert(product)
        } else {
            check(dao.update(product.copy(updatedAt = System.currentTimeMillis())) == 1) {
                "No se encontró el producto"
            }
            product.id
        }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo guardar el producto", it) }
    )

    suspend fun setActive(productId: Long, active: Boolean): AppResult<Unit> = runCatching {
        check(dao.setActive(productId, active, System.currentTimeMillis()) == 1) { "Producto no encontrado" }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo actualizar", it) }
    )
}
