package com.example.apprestaurante.data.remote.dto

data class RecipeResponse(
    val recipes: List<RecipeDto> = emptyList()
)

data class RecipeDto(
    val id: Int,
    val name: String,
    val cuisine: String = "",
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val rating: Double = 0.0,
    val image: String = ""
)
