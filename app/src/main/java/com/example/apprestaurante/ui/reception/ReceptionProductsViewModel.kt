package com.example.apprestaurante.ui.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ReceptionProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val includeInactive = MutableStateFlow(true)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val products = combine(query, includeInactive) { q, include -> q to include }
        .flatMapLatest { (q, include) -> repository.observeProducts(q, "Todos", include) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value.trim()
    }

    fun setIncludeInactive(value: Boolean) {
        includeInactive.value = value
    }

    fun toggle(product: ProductEntity) = viewModelScope.launch {
        _message.value = when (val result = repository.setActive(product.id, !product.isActive)) {
            is AppResult.Success -> if (product.isActive) "Producto desactivado" else "Producto activado"
            is AppResult.Error -> result.message
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
