package com.example.apprestaurante.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.data.repository.CartRepository
import com.example.apprestaurante.data.repository.ProductRepository
import com.example.apprestaurante.data.session.SessionManager
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
class ProductsViewModel(
    private val productsRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val session: SessionManager
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow("Todos")
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val products = combine(query, category) { q, c -> q to c }
        .flatMapLatest { (q, c) -> productsRepository.observeProducts(q, c, false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories = productsRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value.trim()
    }

    fun setCategory(value: String) {
        category.value = value
    }

    fun addToCart(product: ProductEntity) = viewModelScope.launch {
        _message.value = when (
            val result = cartRepository.add(session.userId, product.id)
        ) {
            is AppResult.Success -> "${product.name} agregado al carrito"
            is AppResult.Error -> result.message
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
