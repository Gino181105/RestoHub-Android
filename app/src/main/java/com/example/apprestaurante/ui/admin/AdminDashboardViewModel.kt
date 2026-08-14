package com.example.apprestaurante.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.data.local.model.AdminDashboard
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.repository.ProductRepository
import com.example.apprestaurante.data.repository.SalesDocumentRepository
import com.example.apprestaurante.data.repository.UserManagementRepository
import com.example.apprestaurante.domain.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AdminDashboardViewModel(
    productRepository: ProductRepository,
    orderRepository: OrderRepository,
    userRepository: UserManagementRepository,
    documentRepository: SalesDocumentRepository
) : ViewModel() {
    private data class Counts(
        val products: Int,
        val lowStock: Int,
        val orders: Int,
        val clients: Int,
        val receptionists: Int,
        val administrators: Int
    )

    private val firstCounts = combine(
        productRepository.observeActiveCount(),
        productRepository.observeLowStockCount(),
        orderRepository.observePendingCount(),
        userRepository.observeRoleCount(UserRole.CLIENT.name),
        userRepository.observeRoleCount(UserRole.RECEPTIONIST.name)
    ) { products, lowStock, orders, clients, receptionists ->
        intArrayOf(products, lowStock, orders, clients, receptionists)
    }

    private val counts = combine(
        firstCounts,
        userRepository.observeRoleCount(UserRole.ADMIN.name)
    ) { values, administrators ->
        Counts(
            products = values[0],
            lowStock = values[1],
            orders = values[2],
            clients = values[3],
            receptionists = values[4],
            administrators = administrators
        )
    }

    val dashboard = combine(
        counts,
        documentRepository.observeCount(),
        documentRepository.observeTotalSales()
    ) { count, documents, sales ->
        AdminDashboard(
            activeProducts = count.products,
            lowStockProducts = count.lowStock,
            openOrders = count.orders,
            clients = count.clients,
            receptionists = count.receptionists,
            administrators = count.administrators,
            issuedDocuments = documents,
            totalSales = sales
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AdminDashboard()
    )
}
