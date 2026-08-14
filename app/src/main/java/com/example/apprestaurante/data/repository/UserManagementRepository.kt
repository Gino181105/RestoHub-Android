package com.example.apprestaurante.data.repository

import com.example.apprestaurante.core.AppResult
import com.example.apprestaurante.core.PasswordHasher
import com.example.apprestaurante.data.local.dao.UserDao
import com.example.apprestaurante.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserManagementRepository(
    private val userDao: UserDao
) {
    fun observeUsers(query: String, role: String): Flow<List<UserEntity>> =
        userDao.observeAll(query.trim(), role)

    fun observeRoleCount(role: String): Flow<Int> = userDao.observeRoleCount(role)

    suspend fun getById(id: Long): UserEntity? = userDao.getById(id)

    suspend fun save(user: UserEntity, newPassword: String): AppResult<Long> = runCatching {
        val normalizedEmail = user.email.trim().lowercase()
        val existingEmail = userDao.findByEmail(normalizedEmail)
        check(existingEmail == null || existingEmail.id == user.id) {
            "El correo ya está registrado"
        }

        if (user.id == 0L) {
            require(newPassword.length >= 6) { "La contraseña debe tener al menos 6 caracteres" }
            userDao.insert(
                user.copy(
                    email = normalizedEmail,
                    passwordHash = hashPassword(newPassword),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            val current = userDao.getById(user.id) ?: error("Usuario no encontrado")
            val passwordHash = if (newPassword.isBlank()) {
                current.passwordHash
            } else {
                require(newPassword.length >= 6) { "La contraseña debe tener al menos 6 caracteres" }
                hashPassword(newPassword)
            }
            check(
                userDao.update(
                    user.copy(
                        email = normalizedEmail,
                        passwordHash = passwordHash,
                        createdAt = current.createdAt,
                        updatedAt = System.currentTimeMillis()
                    )
                ) == 1
            ) { "No se pudo actualizar el usuario" }
            user.id
        }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo guardar el usuario", it) }
    )

    suspend fun setActive(userId: Long, active: Boolean): AppResult<Unit> = runCatching {
        check(userDao.setActive(userId, active, System.currentTimeMillis()) == 1) {
            "Usuario no encontrado"
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "No se pudo actualizar", it) }
    )

    private suspend fun hashPassword(password: String): String =
        withContext(Dispatchers.Default) {
            PasswordHasher.hash(password.toCharArray())
        }
}
