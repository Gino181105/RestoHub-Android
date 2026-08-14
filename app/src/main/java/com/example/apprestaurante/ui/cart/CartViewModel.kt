package com.example.apprestaurante.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.model.CartProductItem
import com.example.apprestaurante.data.repository.CartRepository
import com.example.apprestaurante.data.session.SessionManager
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.ServiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class CartUiState(
    val items: List<CartProductItem> = emptyList(),
    val total: Double = 0.0,
    val processing: Boolean = false
)

class CartViewModel(
    private val repository: CartRepository,
    private val session: SessionManager
) : ViewModel() {
    private val processing = MutableStateFlow(false)

    val state: StateFlow<CartUiState> = combine(
        repository.observeCart(session.userId),
        processing
    ) { items, isProcessing ->
        CartUiState(
            items = items,
            total = items.sumOf { it.subtotal },
            processing = isProcessing
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CartUiState()
    )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun increase(item: CartProductItem) = change(item, 1)
    fun decrease(item: CartProductItem) = change(item, -1)

    fun remove(item: CartProductItem) = viewModelScope.launch {
        when (val result = repository.remove(item.cartId)) {
            is AppResult.Success -> _message.value = "Producto retirado"
            is AppResult.Error -> _message.value = result.message
        }
    }

    private fun change(item: CartProductItem, delta: Int) = viewModelScope.launch {
        if (processing.value) return@launch
        when (val result = repository.changeQuantity(item, delta)) {
            is AppResult.Success -> Unit
            is AppResult.Error -> _message.value = result.message
        }
    }

    fun checkout(
        payment: String,
        service: ServiceType,
        notes: String,
        tableNumber: String,
        deliveryAddress: String,
        documentType: DocumentType,
        customerDocument: String,
        businessName: String,
        fiscalAddress: String
    ) = viewModelScope.launch {
        if (processing.value) return@launch
        processing.value = true
        _message.value = when (
            val result = repository.checkout(
                userId = session.userId,
                paymentMethod = payment,
                serviceType = service,
                notes = notes,
                tableNumber = tableNumber,
                deliveryAddress = deliveryAddress,
                documentType = documentType,
                customerDocument = customerDocument,
                businessName = businessName,
                fiscalAddress = fiscalAddress
            )
        ) {
            is AppResult.Success -> "PEDIDO_OK:${result.data}"
            is AppResult.Error -> result.message
        }
        processing.value = false
    }

    fun consumeMessage() {
        _message.value = null
    }
}
