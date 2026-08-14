package com.example.apprestaurante.core

import kotlin.math.round

object TaxCalculator {
    private const val IGV_RATE = 0.18

    fun subtotalFromTotal(total: Double): Double = roundMoney(total / (1.0 + IGV_RATE))

    fun igvFromTotal(total: Double): Double = roundMoney(total - subtotalFromTotal(total))

    fun roundMoney(value: Double): Double = round(value * 100.0) / 100.0
}
