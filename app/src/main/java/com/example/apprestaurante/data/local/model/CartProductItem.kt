package com.example.apprestaurante.data.local.model

data class CartProductItem(
    val cartId: Long,
    val productId: Long,
    val name: String,
    val imageUri: String,
    val price: Double,
    val stock: Int,
    val quantity: Int,
    val subtotal: Double
)
