package com.example.apprestaurante.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {
    @Test
    fun clientCanOnlyCancelEarlyStatuses() {
        assertTrue(OrderStatus.PENDING.canClientCancel())
        assertTrue(OrderStatus.CONFIRMED.canClientCancel())
        assertFalse(OrderStatus.PREPARING.canClientCancel())
        assertFalse(OrderStatus.DELIVERED.canClientCancel())
    }

    @Test
    fun cancelledOrderCanBeDeleted() {
        assertTrue(OrderStatus.CANCELLED.canDelete())
        assertFalse(OrderStatus.DELIVERED.canDelete())
    }
}
