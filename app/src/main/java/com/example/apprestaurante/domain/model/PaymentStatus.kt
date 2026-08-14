package com.example.apprestaurante.domain.model

enum class PaymentStatus(val label: String) {
    PENDING("Pendiente de pago"),
    PAID("Pagado");

    companion object {
        fun from(value: String): PaymentStatus =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}
