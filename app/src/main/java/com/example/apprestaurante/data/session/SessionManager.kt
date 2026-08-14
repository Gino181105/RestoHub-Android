package com.example.apprestaurante.data.session

import android.content.Context
import com.example.apprestaurante.domain.model.UserRole

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences(
        "restohub_session",
        Context.MODE_PRIVATE
    )

    val userId: Long get() = preferences.getLong(KEY_USER_ID, 0L)
    val userName: String get() = preferences.getString(KEY_USER_NAME, "").orEmpty()
    val role: UserRole get() = runCatching {
        UserRole.valueOf(preferences.getString(KEY_ROLE, UserRole.CLIENT.name).orEmpty())
    }.getOrDefault(UserRole.CLIENT)
    val isLoggedIn: Boolean get() = userId > 0L

    fun save(userId: Long, userName: String, role: UserRole) {
        preferences.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_ROLE, role.name)
            .apply()
    }

    fun clear() {
        val version = preferences.getInt(KEY_DATA_VERSION, 0)
        preferences.edit().clear().putInt(KEY_DATA_VERSION, version).apply()
    }

    fun ensureDataVersion(version: Int) {
        val savedVersion = preferences.getInt(KEY_DATA_VERSION, 0)
        if (savedVersion != version) {
            preferences.edit()
                .clear()
                .putInt(KEY_DATA_VERSION, version)
                .apply()
        }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ROLE = "role"
        private const val KEY_DATA_VERSION = "data_version"
    }
}
