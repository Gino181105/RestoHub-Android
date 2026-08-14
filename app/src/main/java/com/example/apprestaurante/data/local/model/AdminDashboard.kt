package com.example.apprestaurante.data.local.model

data class AdminDashboard(
    val activeProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val openOrders: Int = 0,
    val clients: Int = 0,
    val receptionists: Int = 0,
    val administrators: Int = 0,
    val issuedDocuments: Int = 0,
    val totalSales: Double = 0.0
)
