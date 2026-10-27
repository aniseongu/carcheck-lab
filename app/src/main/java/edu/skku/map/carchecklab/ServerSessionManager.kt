package edu.skku.map.carchecklab

import android.content.Context
import android.content.SharedPreferences

class ServerSessionManager {
    companion object {
        private const val PREF_NAME: String = "carcheck_server_session"

        private const val KEY_IS_LOGGED_IN: String = "is_logged_in"
        private const val KEY_USER_ID: String = "current_user_id"
        private const val KEY_CITY: String = "current_city"
        private const val KEY_PRIORITY: String = "current_priority"
        private const val KEY_HAS_PROFILE: String = "has_profile"

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        fun saveSession(
            context: Context,
            userId: String,
            city: String,
            priority: String,
            hasProfile: Boolean
        ) {
            val editor: SharedPreferences.Editor = getPrefs(context).edit()
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.putString(KEY_USER_ID, userId)
            editor.putString(KEY_CITY, city)
            editor.putString(KEY_PRIORITY, priority)
            editor.putBoolean(KEY_HAS_PROFILE, hasProfile)
            editor.apply()
        }

        fun saveProfile(
            context: Context,
            city: String,
            priority: String
        ) {
            val editor: SharedPreferences.Editor = getPrefs(context).edit()
            editor.putString(KEY_CITY, city)
            editor.putString(KEY_PRIORITY, priority)
            editor.putBoolean(KEY_HAS_PROFILE, true)
            editor.apply()
        }

        fun clearSession(context: Context) {
            val editor: SharedPreferences.Editor = getPrefs(context).edit()
            editor.clear()
            editor.apply()
        }

        fun isLoggedIn(context: Context): Boolean {
            return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
        }

        fun getCurrentUserId(context: Context): String {
            return getPrefs(context).getString(KEY_USER_ID, "") ?: ""
        }

        fun getCity(context: Context): String {
            val city: String = getPrefs(context).getString(KEY_CITY, "") ?: ""

            if (city.isEmpty()) {
                return "Suwon"
            }

            return city
        }

        fun getPriority(context: Context): String {
            val priority: String = getPrefs(context).getString(KEY_PRIORITY, "") ?: ""

            if (priority.isEmpty()) {
                return "Safety Nerd"
            }

            return priority
        }

        fun hasProfile(context: Context): Boolean {
            return getPrefs(context).getBoolean(KEY_HAS_PROFILE, false)
        }
    }
}