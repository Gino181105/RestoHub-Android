package com.example.apprestaurante.domain.model

enum class UserRole(val label: String) {
    CLIENT("Cliente"),
    RECEPTIONIST("Recepcionista"),
    ADMIN("Administrador");

    val isStaff: Boolean
        get() = this == RECEPTIONIST || this == ADMIN
}
