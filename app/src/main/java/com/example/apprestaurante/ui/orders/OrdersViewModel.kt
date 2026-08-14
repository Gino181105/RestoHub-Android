package com.example.apprestaurante.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModel(
    private val repository: OrderRepository,
    private val session: SessionManager
) : ViewModel() {
    private val statusFilter = MutableStateFlow("TODOS")

    val clientOrders = repository.observeForClient(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val receptionOrders = statusFilter
        .flatMapLatest(repository::observeAll)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setStatusFilter(value: String) {
        statusFilter.value = value
    }
}
