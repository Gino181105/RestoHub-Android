package com.example.apprestaurante.data.repository

import androidx.room.withTransaction
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.RestoHubDatabase
import com.example.apprestaurante.data.local.entity.OrderEntity
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.local.model.StaffSaleLine
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.PaymentStatus
import com.example.apprestaurante.domain.model.ServiceType

class StaffSaleRepository(
    private val db: RestoHubDatabase,
    private val awaitInitialization: suspend () -> Unit = {}
) {
    private val userDao = db.userDao()
    private val productDao = db.productDao()
    private val orderDao = db.orderDao()

    fun observeProducts() = productDao.observeFiltered("", "Todos", false)

    suspend fun getClients(): List<UserEntity> {
        awaitInitialization()
        return userDao.getActiveClients()
    }

    suspend fun createOrder(
        staffUserId: Long,
        clientUserId: Long,
        lines: List<StaffSaleLine>,
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
        awaitInitialization()
        require(staffUserId > 0L) { "La sesión del personal no es válida" }
        require(clientUserId > 0L) { "Selecciona un cliente" }
        require(lines.isNotEmpty()) { "Agrega al menos un producto" }
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
            val client = userDao.getById(clientUserId) ?: error("Cliente no encontrado")
            check(client.isActive && client.role == "CLIENT") { "El cliente no está disponible" }

            val selectedProducts = lines.map { line ->
                val product = productDao.getById(line.productId)
                    ?: error("Producto no encontrado")
                check(product.isActive) { "${product.name} está inactivo" }
                check(line.quantity > 0) { "Cantidad no válida para ${product.name}" }
                check(line.quantity <= product.stock) { "Stock insuficiente para ${product.name}" }
                product to line.quantity
            }
            val total = selectedProducts.sumOf { (product, quantity) ->
                product.price * quantity
            }

            val orderId = orderDao.insertOrder(
                OrderEntity(
                    userId = clientUserId,
                    createdByUserId = staffUserId,
                    total = total,
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
                    status = OrderStatus.CONFIRMED.name
                )
            )

            orderDao.insertItems(
                selectedProducts.map { (product, quantity) ->
                    OrderItemEntity(
                        orderId = orderId,
                        productId = product.id,
                        productName = product.name,
                        unitPrice = product.price,
                        quantity = quantity,
                        imageUri = product.imageUri
                    )
                }
            )
            selectedProducts.forEach { (product, quantity) ->
                check(
                    productDao.decreaseStock(
                        product.id,
                        quantity,
                        System.currentTimeMillis()
                    ) == 1
                ) { "El stock de ${product.name} cambió" }
            }
            orderId
        }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo registrar la venta", it) }
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
            require(deliveryAddress.trim().length >= 6) { "Ingresa la dirección de delivery" }
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
