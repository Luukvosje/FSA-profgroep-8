package com.profgroep8.rmc_app.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_TOKEN_TIME = "token_saved_time"

        private const val TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_TOKEN_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getToken(): String? {
        return if (isTokenValid()) {
            prefs.getString(KEY_TOKEN, null)
        } else {
            clearSession()
            null
        }
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    private fun isTokenValid(): Boolean {
        val token = prefs.getString(KEY_TOKEN, null) ?: return false
        val savedTime = prefs.getLong(KEY_TOKEN_TIME, 0L)

        if (savedTime == 0L) return false

        val currentTime = System.currentTimeMillis()
        return (currentTime - savedTime) <= TOKEN_VALIDITY_MS
    }

    fun hasValidSession(): Boolean {
        return isTokenValid() && !getUserEmail().isNullOrBlank()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EMAIL)
            .remove(KEY_TOKEN_TIME)
            .apply()
    }
}