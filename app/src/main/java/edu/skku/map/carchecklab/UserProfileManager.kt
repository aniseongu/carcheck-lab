package edu.skku.map.carchecklab

import android.content.Context

class UserProfileManager {
    companion object {
        fun saveProfile(
            context: Context,
            city: String,
            priority: String
        ) {
            ServerSessionManager.saveProfile(context, city, priority)
        }

        fun getCity(context: Context): String {
            return ServerSessionManager.getCity(context)
        }

        fun getPriority(context: Context): String {
            return ServerSessionManager.getPriority(context)
        }

        fun hasProfile(context: Context): Boolean {
            return ServerSessionManager.hasProfile(context)
        }

        fun clearProfile(context: Context) {
            ServerSessionManager.clearSession(context)
        }
    }
}