package com.example.apprestaurante.data.remote

import com.example.apprestaurante.data.remote.dto.RecipeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RestoHubApiService {
    @GET("recipes")
    suspend fun getRecipes(@Query("limit") limit: Int = 8): RecipeResponse
}
