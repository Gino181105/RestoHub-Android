package com.example.apprestaurante.core

import org.junit.Assert.assertEquals
import org.junit.Test

class TaxCalculatorTest {
    @Test
    fun totalIncludesIgv() {
        val total = 118.0
        assertEquals(100.0, TaxCalculator.subtotalFromTotal(total), 0.001)
        assertEquals(18.0, TaxCalculator.igvFromTotal(total), 0.001)
    }

    @Test
    fun subtotalAndIgvReturnOriginalTotal() {
        val total = 79.90
        val calculated = TaxCalculator.subtotalFromTotal(total) + TaxCalculator.igvFromTotal(total)
        assertEquals(total, calculated, 0.001)
    }
}
