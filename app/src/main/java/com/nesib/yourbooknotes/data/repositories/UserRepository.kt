package com.nesib.yourbooknotes.data.repositories

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.models.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(val authApi: AuthApi) {

    suspend fun signup(
        email: String,
        password: String,
        username: String,
    ): Response<UserAuth> {
        val map = mapOf(
            "email" to email,
            "password" to password,
            "username" to username,
        )
        return authApi.signup(map)
    }

    suspend fun signupWithGoogle(
        email: String,
        fullname: String,
        username: String,
        profileImage: String,
    ): Response<UserAuth> {
        val map = mapOf(
            "email" to email,
            "fullname" to fullname,
            "username" to username,
            "profileImage" to profileImage
        )
        return authApi.signupWithGoogle(map)
    }

    suspend fun signInWithGoogle(email:String,profileImage: String): Response<UserAuth> {
        val map = mapOf("email" to email,"profileImage" to profileImage)
        Log.d("mytag", "signInWithGoogle: ${profileImage}")
        return authApi.signInWithGoogle(map)
    }
    suspend fun login(email: String, password: String): Response<UserAuth> {
        val map = mapOf(
            "email" to email,
            "password" to password,
        )
        return authApi.login(map)
    }

    suspend fun followOrUnFollowUser(userId: String) = authApi.followOrUnFollowUser(userId)

    suspend fun saveFollowingGenres(genres: String) =
        authApi.saveFollowingGenres(genres)

    suspend fun updateUser(body:Map<String,String>) = authApi.updateUser(body)

    suspend fun getUser(userId: String) = authApi.getUser(userId)

    suspend fun getMoreUserQuotes(userId: String, page: Int) =
        authApi.getMoreUserQuotes(userId, page)

    suspend fun getUsers(searchQuery: String) = authApi.getUsers(searchQuery)

    suspend fun getSavedQuotes(userId: String) = authApi.getSavedQuotes(userId)

    suspend fun postSavedQuote(quoteId: String) = authApi.postSavedQuote(quoteId)
}