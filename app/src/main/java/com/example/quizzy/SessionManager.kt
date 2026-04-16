package com.example.quizzy

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("QuizzyPrefs", Context.MODE_PRIVATE)

    fun saveUser(id: Long, username: String) {
        prefs.edit().apply {
            putLong("USER_ID", id)
            putString("USERNAME", username)
            apply()
        }
    }

    fun getUserId(): Long = prefs.getLong("USER_ID", -1L)

    fun getUsername(): String? = prefs.getString("USERNAME", null)

    fun isLoggedIn(): Boolean = getUserId() != -1L

    fun saveSelectedChart(chart: String) {
        prefs.edit().putString("SELECTED_CHART", chart).apply()
    }

    fun getSelectedChart(): String? {
        return prefs.getString("SELECTED_CHART", null)
    }

    fun saveSelectedDisplay(display: String) {
        prefs.edit().putString("SELECTED_DISPLAY", display).apply()
    }

    fun getSelectedDisplay(): String? {
        return prefs.getString("SELECTED_DISPLAY", null)
    }

    fun setThemeMode(isDarkMode: Boolean) {
        prefs.edit().putBoolean("IS_DARK_MODE", isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        val systemInDarkTheme = (context.resources.configuration.uiMode and 
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return prefs.getBoolean("IS_DARK_MODE", systemInDarkTheme)
    }

    fun isDarkMode(systemInDarkTheme: Boolean): Boolean {
        return prefs.getBoolean("IS_DARK_MODE", systemInDarkTheme)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}