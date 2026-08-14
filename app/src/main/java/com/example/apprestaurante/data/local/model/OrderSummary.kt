package com.example.apprestaurante.data.local.model

data class OrderSummary(
    val id: Long,
    val userId: Long,
    val clientName: String,
    val clientEmail: String,
    val total: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val serviceType: String,
    val tableNumber: String,
    val deliveryAddress: String,
    val documentType: String,
    val notes: String,
    val status: String,
    val createdAt: Long,
    val itemCount: Long
)
