package com.example.apprestaurante.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.repository.UserManagementRepository
import com.example.apprestaurante.data.session.SessionManager
import com.example.apprestaurante.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class UserFormUiState(
    val loading: Boolean = false,
    val user: UserEntity? = null,
    val message: String? = null,
    val saved: Boolean = false
)

class UserFormViewModel(
    private val repository: UserManagementRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(UserFormUiState())
    val state: StateFlow<UserFormUiState> = _state.asStateFlow()

    fun load(id: Long) = viewModelScope.launch {
        if (id <= 0L) return@launch
        _state.value = _state.value.copy(loading = true)
        _state.value = UserFormUiState(
            user = repository.getById(id),
            loading = false
        )
    }

    fun save(user: UserEntity, password: String) = viewModelScope.launch {
        if (user.id == session.userId && user.role != UserRole.ADMIN.name) {
            _state.value = _state.value.copy(message = "No puedes quitarte el rol Administrador")
            return@launch
        }
        if (user.id == session.userId && !user.isActive) {
            _state.value = _state.value.copy(message = "No puedes desactivar tu propia cuenta")
            return@launch
        }
        _state.value = _state.value.copy(loading = true, message = null)
        _state.value = when (val result = repository.save(user, password)) {
            is AppResult.Success -> _state.value.copy(
                loading = false,
                message = "Usuario guardado",
                saved = true
            )
            is AppResult.Error -> _state.value.copy(loading = false, message = result.message)
        }
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
