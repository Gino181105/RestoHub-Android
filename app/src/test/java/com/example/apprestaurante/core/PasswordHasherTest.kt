package com.example.apprestaurante.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun hash_verifiesCorrectPassword() {
        val hash = PasswordHasher.hash("Cliente123".toCharArray())
        assertTrue(PasswordHasher.verify("Cliente123".toCharArray(), hash))
        assertFalse(PasswordHasher.verify("Incorrecta1".toCharArray(), hash))
    }
}
