package com.example.apprestaurante.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.local.model.StaffSaleLine
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.repository.StaffSaleRepository
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


data class SelectedSaleProduct(
    val product: ProductEntity,
    val quantity: Int
)

data class StaffSaleUiState(
    val products: List<ProductEntity> = emptyList(),
    val clients: List<UserEntity> = emptyList(),
    val selected: List<SelectedSaleProduct> = emptyList(),
    val total: Double = 0.0,
    val processing: Boolean = false
)

class StaffSaleViewModel(
    private val saleRepository: StaffSaleRepository,
    private val orderRepository: OrderRepository,
    private val session: SessionManager
) : ViewModel() {
    private val clients = MutableStateFlow<List<UserEntity>>(emptyList())
    private val selectedQuantities = MutableStateFlow<Map<Long, Int>>(emptyMap())
    private val processing = MutableStateFlow(false)

    private val products = saleRepository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<StaffSaleUiState> = combine(
        products,
        clients,
        selectedQuantities,
        processing
    ) { productList, clientList, quantities, isProcessing ->
        val selected = productList.mapNotNull { product ->
            quantities[product.id]?.takeIf { it > 0 }?.let { quantity ->
                SelectedSaleProduct(product, quantity)
            }
        }
        StaffSaleUiState(
            products = productList,
            clients = clientList,
            selected = selected,
            total = selected.sumOf { it.product.price * it.quantity },
            processing = isProcessing
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StaffSaleUiState()
    )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            clients.value = saleRepository.getClients()
        }
    }

    fun add(product: ProductEntity) {
        val current = selectedQuantities.value[product.id] ?: 0
        if (current >= product.stock) {
            _message.value = "Stock máximo para ${product.name}: ${product.stock}"
            return
        }
        selectedQuantities.value = selectedQuantities.value + (product.id to current + 1)
    }

    fun removeOne(productId: Long) {
        val current = selectedQuantities.value[productId] ?: return
        selectedQuantities.value = if (current <= 1) {
            selectedQuantities.value - productId
        } else {
            selectedQuantities.value + (productId to current - 1)
        }
    }

    fun clearSelection() {
        selectedQuantities.value = emptyMap()
    }

    fun createSale(
        clientId: Long,
        paymentMethod: String,
        serviceType: ServiceType,
        notes: String,
        tableNumber: String,
        deliveryAddress: String,
        documentType: DocumentType,
        customerDocument: String,
        businessName: String,
        fiscalAddress: String,
        payNow: Boolean
    ) = viewModelScope.launch {
        if (processing.value) return@launch
        processing.value = true
        val lines = selectedQuantities.value.map { (productId, quantity) ->
            StaffSaleLine(productId, quantity)
        }
        when (
            val orderResult = saleRepository.createOrder(
                staffUserId = session.userId,
                clientUserId = clientId,
                lines = lines,
                paymentMethod = paymentMethod,
                serviceType = serviceType,
                notes = notes,
                tableNumber = tableNumber,
                deliveryAddress = deliveryAddress,
                documentType = documentType,
                customerDocument = customerDocument,
                businessName = businessName,
                fiscalAddress = fiscalAddress
            )
        ) {
            is AppResult.Error -> _message.value = orderResult.message
            is AppResult.Success -> {
                val orderId = orderResult.data
                if (payNow) {
                    when (
                        val documentResult = orderRepository.markPaidAndIssueDocument(
                            orderId,
                            session.userId,
                            session.role
                        )
                    ) {
                        is AppResult.Success -> {
                            selectedQuantities.value = emptyMap()
                            _message.value = "SALE_OK:$orderId:${documentResult.data}"
                        }
                        is AppResult.Error -> {
                            _message.value = "Pedido #$orderId creado, pero: ${documentResult.message}"
                        }
                    }
                } else {
                    selectedQuantities.value = emptyMap()
                    _message.value = "SALE_OK:$orderId:0"
                }
            }
        }
        processing.value = false
    }

    fun consumeMessage() {
        _message.value = null
    }
}
