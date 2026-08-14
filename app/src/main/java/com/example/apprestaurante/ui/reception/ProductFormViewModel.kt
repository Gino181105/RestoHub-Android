package com.example.apprestaurante.ui.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductFormUiState(
    val loading: Boolean = false,
    val product: ProductEntity? = null,
    val message: String? = null,
    val saved: Boolean = false
)

class ProductFormViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProductFormUiState())
    val state: StateFlow<ProductFormUiState> = _state.asStateFlow()

    fun load(productId: Long) = viewModelScope.launch {
        if (productId <= 0L) return@launch
        _state.value = ProductFormUiState(loading = true)
        val product = repository.getById(productId)
        _state.value = ProductFormUiState(
            product = product,
            message = if (product == null) "Producto no encontrado" else null
        )
    }

    fun save(product: ProductEntity) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, message = null)
        _state.value = when (val result = repository.save(product)) {
            is AppResult.Success -> _state.value.copy(loading = false, saved = true)
            is AppResult.Error -> _state.value.copy(loading = false, message = result.message)
        }
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
