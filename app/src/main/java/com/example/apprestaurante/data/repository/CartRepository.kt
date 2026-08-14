package com.example.apprestaurante.data.repository

import androidx.room.withTransaction
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.RestoHubDatabase
import com.example.apprestaurante.data.local.entity.CartItemEntity
import com.example.apprestaurante.data.local.entity.OrderEntity
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.model.CartProductItem
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.PaymentStatus
import com.example.apprestaurante.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow

class CartRepository(private val db: RestoHubDatabase) {
    private val cartDao = db.cartDao()
    private val productDao = db.productDao()
    private val orderDao = db.orderDao()

    fun observeCart(userId: Long): Flow<List<CartProductItem>> = cartDao.observeCart(userId)
    fun observeCount(userId: Long): Flow<Int> = cartDao.observeItemCount(userId)

    suspend fun add(userId: Long, productId: Long): AppResult<Unit> {
        if (userId <= 0L) return AppResult.Error("La sesión no es válida")
        val product = productDao.getById(productId)
            ?: return AppResult.Error("Producto no encontrado")
        if (!product.isActive || product.stock <= 0) {
            return AppResult.Error("Producto sin stock")
        }

        return runCatching {
            val current = cartDao.findItem(userId, productId)
            if (current == null) {
                cartDao.insert(
                    CartItemEntity(
                        userId = userId,
                        productId = productId,
                        quantity = 1
                    )
                )
            } else {
                check(current.quantity < product.stock) { "No hay más unidades disponibles" }
                check(cartDao.updateQuantity(current.id, current.quantity + 1) == 1) {
                    "No se pudo actualizar el carrito"
                }
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo agregar", it) }
        )
    }

    suspend fun changeQuantity(item: CartProductItem, delta: Int): AppResult<Unit> = runCatching {
        val next = item.quantity + delta
        when {
            next <= 0 -> check(cartDao.delete(item.cartId) == 1) { "Producto no encontrado" }
            next > item.stock -> error("Stock máximo: ${item.stock}")
            else -> check(cartDao.updateQuantity(item.cartId, next) == 1) {
                "No se pudo actualizar la cantidad"
            }
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo cambiar la cantidad", it) }
    )

    suspend fun remove(cartId: Long): AppResult<Unit> = runCatching {
        check(cartDao.delete(cartId) == 1) { "Producto no encontrado" }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo quitar el producto", it) }
    )

    suspend fun checkout(
        userId: Long,
        paymentMethod: String,
        serviceType: ServiceType,
        notes: String,
        tableNumber: String,
        deliveryAddress: String,
        documentType: DocumentType,
        customerDocument: String,
        businessName: String,
        fiscalAddress: String
    ): AppResult<Long> = runCatching {
        require(userId > 0L) { "La sesión no es válida" }
        validateCheckout(
            serviceType,
            tableNumber,
            deliveryAddress,
            documentType,
            customerDocument,
            businessName,
            fiscalAddress
        )

        db.withTransaction {
            val cart = cartDao.getCart(userId)
            require(cart.isNotEmpty()) { "El carrito está vacío" }
            cart.forEach { item ->
                check(item.quantity > 0) { "Cantidad no válida" }
                check(item.quantity <= item.stock) { "Stock insuficiente para ${item.name}" }
            }

            val orderId = orderDao.insertOrder(
                OrderEntity(
                    userId = userId,
                    createdByUserId = userId,
                    total = cart.sumOf { it.subtotal },
                    paymentMethod = paymentMethod,
                    serviceType = serviceType.label,
                    notes = notes.trim(),
                    tableNumber = tableNumber.trim(),
                    deliveryAddress = deliveryAddress.trim(),
                    documentType = documentType.name,
                    customerDocument = customerDocument.trim(),
                    businessName = businessName.trim(),
                    fiscalAddress = fiscalAddress.trim(),
                    paymentStatus = PaymentStatus.PENDING.name,
                    status = OrderStatus.PENDING.name
                )
            )

            orderDao.insertItems(
                cart.map { item ->
                    OrderItemEntity(
                        orderId = orderId,
                        productId = item.productId,
                        productName = item.name,
                        unitPrice = item.price,
                        quantity = item.quantity,
                        imageUri = item.imageUri
                    )
                }
            )

            cart.forEach { item ->
                check(
                    productDao.decreaseStock(
                        item.productId,
                        item.quantity,
                        System.currentTimeMillis()
                    ) == 1
                ) { "El stock de ${item.name} cambió. Intenta nuevamente" }
            }
            cartDao.clear(userId)
            orderId
        }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo registrar el pedido", it) }
    )

    private fun validateCheckout(
        serviceType: ServiceType,
        tableNumber: String,
        deliveryAddress: String,
        documentType: DocumentType,
        customerDocument: String,
        businessName: String,
        fiscalAddress: String
    ) {
        if (serviceType == ServiceType.TABLE) {
            require(tableNumber.isNotBlank()) { "Ingresa el número de mesa" }
        }
        if (serviceType == ServiceType.DELIVERY) {
            require(deliveryAddress.trim().length >= 6) { "Ingresa una dirección de delivery válida" }
        }
        when (documentType) {
            DocumentType.BOLETA -> {
                require(customerDocument.isBlank() || customerDocument.matches(Regex("\\d{8}"))) {
                    "El DNI debe tener 8 dígitos"
                }
            }
            DocumentType.FACTURA -> {
                require(customerDocument.matches(Regex("\\d{11}"))) { "El RUC debe tener 11 dígitos" }
                require(businessName.trim().length >= 3) { "Ingresa la razón social" }
                require(fiscalAddress.trim().length >= 6) { "Ingresa la dirección fiscal" }
            }
        }
    }
}
