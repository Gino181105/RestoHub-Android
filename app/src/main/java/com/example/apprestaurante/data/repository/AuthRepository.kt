package com.example.apprestaurante.data.repository

import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.core.PasswordHasher
import com.example.apprestaurante.data.local.dao.UserDao
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.data.session.SessionManager
import com.example.apprestaurante.domain.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val session: SessionManager,
    private val awaitInitialization: suspend () -> Unit
) {
    suspend fun registerClient(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): AppResult<Long> {
        try {
            awaitInitialization()
        } catch (error: Throwable) {
            return AppResult.Error("No se pudo iniciar la base de datos", error)
        }
        val normalizedEmail = email.trim().lowercase()
        if (userDao.findByEmail(normalizedEmail) != null) {
            return AppResult.Error("El correo ya está registrado")
        }

        return runCatching {
            val id = userDao.insert(
                UserEntity(
                    fullName = fullName.trim(),
                    email = normalizedEmail,
                    phone = phone.trim(),
                    passwordHash = hashPassword(password),
                    role = UserRole.CLIENT.name
                )
            )
            session.save(id, fullName.trim(), UserRole.CLIENT)
            id
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo registrar", it) }
        )
    }

    suspend fun login(email: String, password: String): AppResult<UserEntity> {
        try {
            awaitInitialization()
        } catch (error: Throwable) {
            return AppResult.Error("No se pudo iniciar la base de datos", error)
        }
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return AppResult.Error("Correo o contraseña incorrectos")

        if (!user.isActive) {
            return AppResult.Error("La cuenta está desactivada. Comunícate con el administrador")
        }

        if (!verifyPassword(password, user.passwordHash)) {
            return AppResult.Error("Correo o contraseña incorrectos")
        }

        val role = runCatching { UserRole.valueOf(user.role) }
            .getOrDefault(UserRole.CLIENT)
        session.save(user.id, user.fullName, role)
        return AppResult.Success(user)
    }

    /**
     * Vincula un usuario ya autenticado por Firebase/Google con la base local de RestoHub.
     * Si el correo ya existe, conserva su rol (CLIENT, RECEPTIONIST o ADMIN).
     * Si es nuevo, se crea como CLIENT.
     */
    suspend fun loginWithGoogle(
        fullName: String,
        email: String,
        photoUri: String?
    ): AppResult<UserEntity> {
        try {
            awaitInitialization()
        } catch (error: Throwable) {
            return AppResult.Error("No se pudo iniciar la base de datos", error)
        }

        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) {
            return AppResult.Error("Google no devolvió un correo válido")
        }

        return runCatching {
            val existing = userDao.findByEmail(normalizedEmail)
            val user = if (existing != null) {
                if (!existing.isActive) {
                    error("La cuenta está desactivada. Comunícate con el administrador")
                }
                val updated = existing.copy(
                    fullName = fullName.trim().ifBlank { existing.fullName },
                    photoUri = photoUri ?: existing.photoUri,
                    updatedAt = System.currentTimeMillis()
                )
                userDao.update(updated)
                updated
            } else {
                val generatedPassword = UUID.randomUUID().toString() + UUID.randomUUID().toString()
                val id = userDao.insert(
                    UserEntity(
                        fullName = fullName.trim().ifBlank { normalizedEmail.substringBefore('@') },
                        email = normalizedEmail,
                        passwordHash = hashPassword(generatedPassword),
                        role = UserRole.CLIENT.name,
                        photoUri = photoUri
                    )
                )
                userDao.getById(id) ?: error("No se pudo crear el usuario local")
            }

            val role = runCatching { UserRole.valueOf(user.role) }
                .getOrDefault(UserRole.CLIENT)
            session.save(user.id, user.fullName, role)
            user
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { error ->
                AppResult.Error(error.message ?: "No se pudo iniciar sesión con Google", error)
            }
        )
    }

    suspend fun resetPassword(email: String, newPassword: String): AppResult<Unit> {
        try {
            awaitInitialization()
        } catch (error: Throwable) {
            return AppResult.Error("No se pudo iniciar la base de datos", error)
        }
        val normalizedEmail = email.trim().lowercase()
        if (userDao.findByEmail(normalizedEmail) == null) {
            return AppResult.Error("No existe una cuenta con ese correo")
        }
        val rows = userDao.updatePassword(
            normalizedEmail,
            hashPassword(newPassword),
            System.currentTimeMillis()
        )
        return if (rows == 1) {
            AppResult.Success(Unit)
        } else {
            AppResult.Error("No se pudo actualizar la contraseña")
        }
    }

    fun logout() {
        runCatching { FirebaseAuth.getInstance().signOut() }
        session.clear()
    }

    private suspend fun hashPassword(password: String): String =
        withContext(Dispatchers.Default) {
            PasswordHasher.hash(password.toCharArray())
        }

    private suspend fun verifyPassword(password: String, storedHash: String): Boolean =
        withContext(Dispatchers.Default) {
            PasswordHasher.verify(password.toCharArray(), storedHash)
        }
}
