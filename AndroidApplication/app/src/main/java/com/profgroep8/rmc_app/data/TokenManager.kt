package com.profgroep8.rmc_app.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun clearToken() {
        prefs.edit()
            .remove("auth_token")
            .remove("user_email")
            .apply()
    }

    fun hasValidSession(): Boolean {
        val token = getToken()
        val email = getUserEmail()
        return !token.isNullOrBlank() && !email.isNullOrBlank()
    }
}