package com.example.apprestaurante.ui.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.data.local.dao.UserDao
import com.example.apprestaurante.data.local.model.ReceptionDashboard
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ReceptionDashboardViewModel(
    productRepository: ProductRepository,
    orderRepository: OrderRepository,
    userDao: UserDao
) : ViewModel() {
    val dashboard = combine(
        productRepository.observeActiveCount(),
        productRepository.observeLowStockCount(),
        orderRepository.observePendingCount(),
        userDao.observeClientCount(),
        orderRepository.observeDeliveredSales()
    ) { activeProducts, lowStock, pendingOrders, clients, sales ->
        ReceptionDashboard(
            activeProducts = activeProducts,
            lowStockProducts = lowStock,
            pendingOrders = pendingOrders,
            clientCount = clients,
            deliveredSales = sales
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReceptionDashboard()
    )
}
