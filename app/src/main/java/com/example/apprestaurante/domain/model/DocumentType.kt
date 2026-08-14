package com.example.apprestaurante.domain.model

enum class DocumentType(
    val label: String,
    val series: String
) {
    BOLETA("Boleta de venta", "B001"),
    FACTURA("Factura", "F001");

    companion object {
        fun from(value: String): DocumentType =
            entries.firstOrNull { it.name == value } ?: BOLETA
    }
}
