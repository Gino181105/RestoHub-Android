package com.example.apprestaurante.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.OrderEntity
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.session.SessionManager
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class OrderDetailUiState(
    val loading: Boolean = true,
    val order: OrderEntity? = null,
    val client: UserEntity? = null,
    val items: List<OrderItemEntity> = emptyList(),
    val document: SalesDocumentEntity? = null,
    val message: String? = null,
    val close: Boolean = false
)

class OrderDetailViewModel(
    private val repository: OrderRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(OrderDetailUiState())
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    fun load(orderId: Long) = viewModelScope.launch {
        val order = repository.getOrder(orderId)
        if (order == null) {
            _state.value = OrderDetailUiState(loading = false, message = "Pedido no encontrado")
            return@launch
        }
        if (session.role == UserRole.CLIENT && order.userId != session.userId) {
            _state.value = OrderDetailUiState(loading = false, message = "No tienes permiso")
            return@launch
        }
        _state.value = OrderDetailUiState(
            loading = false,
            order = order,
            client = repository.getUser(order.userId),
            items = repository.getItems(orderId),
            document = repository.getDocument(orderId)
        )
    }

    fun cancelClient() = viewModelScope.launch {
        val order = _state.value.order ?: return@launch
        runAction(repository.cancelByClient(order.id, session.userId))
    }

    fun updateStaff(status: OrderStatus) = viewModelScope.launch {
        val order = _state.value.order ?: return@launch
        runAction(repository.updateByStaff(order.id, status, session.role))
    }

    fun markPaid() = viewModelScope.launch {
        val order = _state.value.order ?: return@launch
        when (
            val result = repository.markPaidAndIssueDocument(
                order.id,
                session.userId,
                session.role
            )
        ) {
            is AppResult.Success -> {
                reload(order.id, "DOCUMENT_OK:${result.data}")
            }
            is AppResult.Error -> _state.value = _state.value.copy(message = result.message)
        }
    }

    fun delete() = viewModelScope.launch {
        val order = _state.value.order ?: return@launch
        val result = repository.delete(
            orderId = order.id,
            requesterId = session.userId,
            requesterRole = session.role
        )
        _state.value = when (result) {
            is AppResult.Success -> _state.value.copy(message = "Pedido borrado", close = true)
            is AppResult.Error -> _state.value.copy(message = result.message)
        }
    }

    private suspend fun runAction(result: AppResult<Unit>) {
        when (result) {
            is AppResult.Success -> {
                val id = _state.value.order?.id ?: return
                reload(id, "Pedido actualizado")
            }
            is AppResult.Error -> _state.value = _state.value.copy(message = result.message)
        }
    }

    private suspend fun reload(orderId: Long, message: String) {
        _state.value = _state.value.copy(
            order = repository.getOrder(orderId),
            document = repository.getDocument(orderId),
            message = message
        )
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
