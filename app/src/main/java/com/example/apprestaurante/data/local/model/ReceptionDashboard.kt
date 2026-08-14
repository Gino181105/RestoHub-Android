package com.example.apprestaurante.data.local.model

data class ReceptionDashboard(
    val activeProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val pendingOrders: Int = 0,
    val clientCount: Int = 0,
    val deliveredSales: Double = 0.0
)
