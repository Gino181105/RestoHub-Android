package com.example.apprestaurante.core

object Validation {
    private val emailRegex = Regex(
        pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        option = RegexOption.IGNORE_CASE
    )

    fun name(value: String): String? = when {
        value.isBlank() -> "El nombre es obligatorio"
        value.trim().length < 3 -> "Ingresa al menos 3 caracteres"
        else -> null
    }

    fun email(value: String): String? = when {
        value.isBlank() -> "El correo es obligatorio"
        !emailRegex.matches(value.trim()) -> "Ingresa un correo válido"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> "La contraseña es obligatoria"
        value.length < 6 -> "Usa al menos 6 caracteres"
        value.none(Char::isLetter) -> "Incluye al menos una letra"
        value.none(Char::isDigit) -> "Incluye al menos un número"
        else -> null
    }
}
