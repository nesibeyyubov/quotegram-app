package com.nesib.yourbooknotes.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.nesib.yourbooknotes.models.UserAuth
import com.nesib.yourbooknotes.utils.toJoinedString
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

class SharedPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
    @Named("encryptedSharedPreferences") val sharedPreferences: SharedPreferences
) {
    private val editor = sharedPreferences.edit()
    private var _currentUser: UserAuth? = null

    fun getCurrentUser(): UserAuth {
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val profileImage = sharedPreferences.getString("profileImage", "")
        val userId = sharedPreferences.getString("userId", null)
        val token = sharedPreferences.getString("token", null)
        val followingGenres = sharedPreferences.getString("genres", "") ?: ""
        _currentUser = UserAuth(
            username = username,
            email = email,
            profileImage = profileImage,
            userId = userId,
            token = token,
            followingGenres = followingGenres.split(",").toList()
        )
        return _currentUser!!
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun saveUser(user: UserAuth) {
        editor.putString("userId", user.userId)
        editor.putString("token", user.token)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putString("profileImage", user.profileImage)
        if (user.followingGenres!!.isNotEmpty()) {
            editor.putString("genres", user.followingGenres.toJoinedString())
        }
        editor.apply()
    }

    fun updateUser(user:UserAuth){
        editor.putString("username", user.username)
    }

    fun getFollowingGenres(): String {
        return sharedPreferences.getString("genres", "")!!
    }

    fun clearUser() {
        editor.remove("userId")
        editor.remove("token")
        editor.remove("genres")
        editor.remove("username")
        editor.remove("email")
        editor.remove("profileImage")
        editor.apply()
    }

    fun saveFollowingGenres(genres: String) {
        editor.putString("genres", genres)
        editor.apply()
    }


}