package com.example.apprestaurante.data.repository

import androidx.room.withTransaction
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.core.TaxCalculator
import com.example.apprestaurante.data.local.RestoHubDatabase
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.PaymentStatus
import com.example.apprestaurante.domain.model.UserRole

class OrderRepository(private val db: RestoHubDatabase) {
    private val orderDao = db.orderDao()
    private val productDao = db.productDao()
    private val documentDao = db.salesDocumentDao()

    fun observeForClient(userId: Long) = orderDao.observeForClient(userId)
    fun observeAll(status: String) = orderDao.observeAll(status)
    fun observePendingCount() = orderDao.observePendingCount()
    fun observeDeliveredSales() = orderDao.observeDeliveredSales()

    suspend fun getOrder(orderId: Long) = orderDao.getById(orderId)
    suspend fun getItems(orderId: Long) = orderDao.getItems(orderId)
    suspend fun getUser(userId: Long) = db.userDao().getById(userId)
    suspend fun getDocument(orderId: Long) = documentDao.findByOrder(orderId)

    suspend fun cancelByClient(orderId: Long, userId: Long): AppResult<Unit> = runCatching {
        db.withTransaction {
            val order = orderDao.getForClient(orderId, userId)
                ?: error("Pedido no encontrado")
            val current = OrderStatus.from(order.status)
            check(current.canClientCancel()) { "El pedido ya no puede cancelarse" }
            check(PaymentStatus.from(order.paymentStatus) == PaymentStatus.PENDING) {
                "Un pedido pagado debe ser anulado por el administrador"
            }
            restoreStock(orderId)
            check(
                orderDao.updateStatus(
                    orderId,
                    OrderStatus.CANCELLED.name,
                    System.currentTimeMillis()
                ) == 1
            ) { "No se pudo cancelar" }
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo cancelar", it) }
    )

    suspend fun updateByStaff(
        orderId: Long,
        newStatus: OrderStatus,
        staffRole: UserRole
    ): AppResult<Unit> = runCatching {
        require(staffRole.isStaff) { "No tienes permiso para actualizar pedidos" }
        db.withTransaction {
            val order = orderDao.getById(orderId) ?: error("Pedido no encontrado")
            val current = OrderStatus.from(order.status)
            check(newStatus in current.nextForReceptionist()) {
                "Cambio de estado no permitido"
            }
            if (newStatus == OrderStatus.CANCELLED) {
                check(PaymentStatus.from(order.paymentStatus) == PaymentStatus.PENDING) {
                    "El pedido ya fue pagado. La anulación debe hacerse fuera de este flujo"
                }
                restoreStock(orderId)
            }
            if (newStatus == OrderStatus.DELIVERED) {
                check(PaymentStatus.from(order.paymentStatus) == PaymentStatus.PAID) {
                    "Primero registra el pago y genera el comprobante"
                }
            }
            check(
                orderDao.updateStatus(orderId, newStatus.name, System.currentTimeMillis()) == 1
            ) { "No se pudo actualizar el pedido" }
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo actualizar", it) }
    )

    suspend fun markPaidAndIssueDocument(
        orderId: Long,
        issuerUserId: Long,
        staffRole: UserRole
    ): AppResult<Long> = runCatching {
        require(staffRole.isStaff) { "No tienes permiso para cobrar pedidos" }
        db.withTransaction {
            documentDao.findByOrder(orderId)?.let { return@withTransaction it.id }

            val order = orderDao.getById(orderId) ?: error("Pedido no encontrado")
            val status = OrderStatus.from(order.status)
            check(status != OrderStatus.CANCELLED) { "No se puede cobrar un pedido cancelado" }
            check(PaymentStatus.from(order.paymentStatus) == PaymentStatus.PENDING) {
                "El pedido ya fue pagado"
            }
            val client = db.userDao().getById(order.userId) ?: error("Cliente no encontrado")
            val type = DocumentType.from(order.documentType)
            val nextNumber = documentDao.nextNumber(type.series)
            val subtotal = TaxCalculator.subtotalFromTotal(order.total)
            val igv = TaxCalculator.igvFromTotal(order.total)

            val documentId = documentDao.insert(
                SalesDocumentEntity(
                    orderId = order.id,
                    documentType = type.name,
                    series = type.series,
                    number = nextNumber,
                    clientName = if (type == DocumentType.FACTURA && order.businessName.isNotBlank()) {
                        order.businessName
                    } else {
                        client.fullName
                    },
                    clientDocument = order.customerDocument,
                    businessName = order.businessName,
                    fiscalAddress = order.fiscalAddress,
                    subtotal = subtotal,
                    igv = igv,
                    total = order.total,
                    paymentMethod = order.paymentMethod,
                    issuerUserId = issuerUserId
                )
            )
            check(orderDao.markPaid(order.id, System.currentTimeMillis()) == 1) {
                "No se pudo registrar el pago"
            }
            documentId
        }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo generar el comprobante", it) }
    )

    suspend fun delete(
        orderId: Long,
        requesterId: Long?,
        requesterRole: UserRole
    ): AppResult<Unit> = runCatching {
        db.withTransaction {
            val order = orderDao.getById(orderId) ?: error("Pedido no encontrado")
            if (requesterRole == UserRole.CLIENT) {
                check(order.userId == requesterId) { "No tienes permiso" }
            } else {
                check(requesterRole.isStaff) { "No tienes permiso" }
            }
            check(OrderStatus.from(order.status).canDelete()) {
                "Solo se borran pedidos cancelados"
            }
            check(documentDao.findByOrder(orderId) == null) {
                "No se puede borrar un pedido con comprobante emitido"
            }
            check(orderDao.delete(orderId) == 1) { "No se pudo borrar" }
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo borrar", it) }
    )

    private suspend fun restoreStock(orderId: Long) {
        orderDao.getItems(orderId).forEach { item ->
            check(
                productDao.increaseStock(
                    item.productId,
                    item.quantity,
                    System.currentTimeMillis()
                ) == 1
            ) { "No se pudo devolver el stock de ${item.productName}" }
        }
    }
}
