package com.example.apprestaurante.core

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 65_536
    private const val KEY_LENGTH = 256

    fun hash(password: CharArray): String {
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
        spec.clearPassword()
        return "${Base64.getEncoder().encodeToString(salt)}:${Base64.getEncoder().encodeToString(hash)}"
    }

    fun verify(password: CharArray, stored: String): Boolean = runCatching {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = Base64.getDecoder().decode(parts[0])
        val expected = Base64.getDecoder().decode(parts[1])
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
        spec.clearPassword()
        java.security.MessageDigest.isEqual(expected, actual)
    }.getOrDefault(false)
}
