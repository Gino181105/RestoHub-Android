package com.example.apprestaurante.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationTest {
    @Test fun validEmail_hasNoError() {
        assertNull(Validation.email("usuario@restohub.pe"))
    }

    @Test fun invalidEmail_returnsError() {
        assertEquals("Ingresa un correo válido", Validation.email("correo-invalido"))
    }

    @Test fun password_requiresLettersAndNumbers() {
        assertEquals("Incluye al menos un número", Validation.password("abcdef"))
        assertEquals("Incluye al menos una letra", Validation.password("123456"))
        assertNull(Validation.password("Resto123"))
    }
}
