package com.example.apprestaurante.data.repository

import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.remote.RestoHubApiService
import com.example.apprestaurante.data.remote.dto.RecipeDto

class RemoteRepository(private val api: RestoHubApiService) {
    suspend fun recipes(): AppResult<List<RecipeDto>> = runCatching {
        api.getRecipes().recipes
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error("Sin conexión. El menú local sigue disponible", it) }
    )
}
