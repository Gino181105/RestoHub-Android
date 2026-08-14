package com.example.apprestaurante.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthUiState(loading = true)
        _state.value = when (val result = repository.login(email, password)) {
            is AppResult.Success -> AuthUiState(success = true)
            is AppResult.Error -> AuthUiState(message = result.message)
        }
    }

    fun loginWithGoogle(fullName: String, email: String, photoUri: String?) =
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            _state.value = when (
                val result = repository.loginWithGoogle(fullName, email, photoUri)
            ) {
                is AppResult.Success -> AuthUiState(success = true)
                is AppResult.Error -> AuthUiState(message = result.message)
            }
        }

    fun register(name: String, email: String, phone: String, password: String) =
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            _state.value = when (
                val result = repository.registerClient(name, email, phone, password)
            ) {
                is AppResult.Success -> AuthUiState(success = true)
                is AppResult.Error -> AuthUiState(message = result.message)
            }
        }

    fun resetPassword(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthUiState(loading = true)
        _state.value = when (val result = repository.resetPassword(email, password)) {
            is AppResult.Success -> AuthUiState(
                success = true,
                message = "Contraseña actualizada correctamente"
            )
            is AppResult.Error -> AuthUiState(message = result.message)
        }
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
