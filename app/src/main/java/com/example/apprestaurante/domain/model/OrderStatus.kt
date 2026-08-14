package com.example.apprestaurante.domain.model

enum class OrderStatus(val label: String) {
    PENDING("Pendiente"),
    CONFIRMED("Confirmado"),
    PREPARING("En preparación"),
    READY("Listo para entregar"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    fun canClientCancel(): Boolean = this == PENDING || this == CONFIRMED
    fun canDelete(): Boolean = this == CANCELLED

    fun nextForReceptionist(): List<OrderStatus> = when (this) {
        PENDING -> listOf(CONFIRMED, CANCELLED)
        CONFIRMED -> listOf(PREPARING, CANCELLED)
        PREPARING -> listOf(READY, CANCELLED)
        READY -> listOf(DELIVERED)
        DELIVERED, CANCELLED -> emptyList()
    }

    companion object {
        fun from(value: String): OrderStatus =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}
