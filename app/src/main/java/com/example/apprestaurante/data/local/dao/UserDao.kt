package com.example.apprestaurante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apprestaurante.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity): Int

    @Query("SELECT * FROM users WHERE lower(email) = lower(:email) LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("""
        SELECT * FROM users
        WHERE (:role = 'TODOS' OR role = :role)
          AND (fullName LIKE '%' || :query || '%'
               OR email LIKE '%' || :query || '%'
               OR documentNumber LIKE '%' || :query || '%')
        ORDER BY isActive DESC, role, fullName COLLATE NOCASE
    """)
    fun observeAll(query: String, role: String): Flow<List<UserEntity>>

    @Query("UPDATE users SET passwordHash = :passwordHash, updatedAt = :updatedAt WHERE lower(email) = lower(:email)")
    suspend fun updatePassword(email: String, passwordHash: String, updatedAt: Long): Int

    @Query("UPDATE users SET photoUri = :photoUri, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updatePhoto(userId: Long, photoUri: String?, updatedAt: Long): Int

    @Query("UPDATE users SET isActive = :active, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun setActive(userId: Long, active: Boolean, updatedAt: Long): Int

    @Query("SELECT * FROM users WHERE role = 'CLIENT' AND isActive = 1 ORDER BY fullName COLLATE NOCASE")
    suspend fun getActiveClients(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    fun observeRoleCount(role: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'CLIENT'")
    fun observeClientCount(): Flow<Int>
}
