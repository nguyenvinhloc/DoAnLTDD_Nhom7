package com.example.doannhom7.utils

import android.content.Context
import com.example.doannhom7.data.entity.UserRole

class SessionManager(ctx: Context) {

    private val pref = ctx.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveLogin(userId: Long, role: UserRole) {
        pref.edit()
            .putBoolean("logged_in", true)
            .putLong("user_id", userId)
            .putString("role", role.name)
            .apply()
    }

    fun isLoggedIn(): Boolean = pref.getBoolean("logged_in", false)

    fun getUserId(): Long = pref.getLong("user_id", -1)

    fun getRole(): UserRole? {
        val r = pref.getString("role", null) ?: return null
        return try { UserRole.valueOf(r) } catch (_: Exception) { null }
    }

    fun logout() {
        pref.edit().clear().apply()
    }
}
