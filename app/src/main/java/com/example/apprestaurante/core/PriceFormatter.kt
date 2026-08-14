package com.example.apprestaurante.core

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    private val locale = Locale.forLanguageTag("es-PE")

    fun format(value: Double): String =
        NumberFormat.getCurrencyInstance(locale).format(value)
}
