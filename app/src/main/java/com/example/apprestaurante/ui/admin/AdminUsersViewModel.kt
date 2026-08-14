package com.example.apprestaurante.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.repository.UserManagementRepository
import com.example.apprestaurante.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AdminUsersViewModel(
    private val repository: UserManagementRepository,
    private val session: SessionManager
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val role = MutableStateFlow("TODOS")

    val users = combine(query, role) { text, selectedRole -> text to selectedRole }
        .flatMapLatest { (text, selectedRole) -> repository.observeUsers(text, selectedRole) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setQuery(value: String) {
        query.value = value
    }

    fun setRole(value: String) {
        role.value = value
    }

    fun toggle(user: UserEntity) = viewModelScope.launch {
        if (user.id == session.userId) {
            _message.value = "No puedes desactivar tu propia cuenta"
            return@launch
        }
        _message.value = when (val result = repository.setActive(user.id, !user.isActive)) {
            is AppResult.Success -> if (user.isActive) "Usuario desactivado" else "Usuario activado"
            is AppResult.Error -> result.message
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
