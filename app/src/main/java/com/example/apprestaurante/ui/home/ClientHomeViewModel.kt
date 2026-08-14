package com.example.apprestaurante.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.remote.dto.RecipeDto
import com.example.apprestaurante.data.repository.OrderRepository
import com.example.apprestaurante.data.repository.RemoteRepository
import com.example.apprestaurante.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClientHomeUiState(
    val loading: Boolean = false,
    val recipes: List<RecipeDto> = emptyList(),
    val message: String? = null
)

class ClientHomeViewModel(
    private val remoteRepository: RemoteRepository,
    orderRepository: OrderRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(ClientHomeUiState())
    val state: StateFlow<ClientHomeUiState> = _state.asStateFlow()

    val orderCount = orderRepository.observeForClient(session.userId)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, message = null)
        _state.value = when (val result = remoteRepository.recipes()) {
            is AppResult.Success -> ClientHomeUiState(recipes = result.data)
            is AppResult.Error -> ClientHomeUiState(message = result.message)
        }
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
