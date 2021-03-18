package com.nesib.yourbooknotes.data.local

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.nesib.yourbooknotes.models.AuthResponse
import com.nesib.yourbooknotes.models.User
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesRepository @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun saveUser(userId: String, token: String) {
        editor.putString("userId", userId)
        editor.putString("token", token)
        editor.apply()
    }

    fun saveExtraUserDetail(username: String, email: String, profileImage: String) {
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("profileImage", profileImage)
        editor.apply()
    }

    fun getExtraUserDetail(): String {
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val profileImage = sharedPreferences.getString("profileImage", "")
        return "$username,$email,$profileImage"
    }

    fun getUser(): AuthResponse {
        val userId = sharedPreferences.getString("userId", null)
        val token = sharedPreferences.getString("token", null)
        return AuthResponse(userId, token)
    }

    fun clearUser() {
        editor.remove("userId")
        editor.remove("token")
        editor.remove("genres")
        editor.apply()
    }

    fun saveFollowingGenres(genres: String) {
        editor.putString("genres", genres)
        editor.apply()
    }

    fun getFollowingGenres(): String {
        return sharedPreferences.getString("genres", "")!!
    }


}