package com.example.apprestaurante.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprestaurante.data.local.dao.UserDao
import com.example.apprestaurante.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userDao: UserDao,
    private val session: SessionManager
) : ViewModel() {
    val user = userDao.observeById(session.userId)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun savePhoto(uri: String) = viewModelScope.launch {
        if (userDao.updatePhoto(session.userId, uri, System.currentTimeMillis()) == 1) {
            _message.value = "Foto actualizada"
        } else {
            _message.value = "No se pudo actualizar la foto"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
