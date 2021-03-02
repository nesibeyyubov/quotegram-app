package com.nesib.yourbooknotes.data.repositories

import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.models.AuthResponse
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap

object UserRepository {
    private val retrofit =
        Retrofit.Builder().baseUrl("http://10.0.2.2:4000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val authApi = retrofit.create(AuthApi::class.java)


    suspend fun signup(
        email: String,
        password: String,
        fullname: String,
        username: String
    ): Response<AuthResponse> {
        val map = mapOf(
            "email" to email,
            "password" to password,
            "fullname" to fullname,
            "username" to username
        )
        return authApi.signup(map)
    }

    suspend fun login(email: String, password: String): Response<AuthResponse> {
        val map = mapOf(
            "email" to email,
            "password" to password,
        )
        return authApi.login(map)
    }

    suspend fun followOrUnfollowUser(userId: String) {
        return authApi.followOrUnfollowUser(userId)
    }

    suspend fun saveFollowingGenres(genres: Map<String, String>): Response<User> {
        return authApi.saveFollowingGenres(genres)
    }

    suspend fun updateUser(
        username: String?,
        fullname: String?,
        email: String?,
        password: String?,
        bio: String?
    ): Response<User> {
        return authApi.updateUser(username, fullname, email, password, bio)
    }

    suspend fun getUser(userId: String): Response<User> {
        return authApi.getUser(userId)
    }

    suspend fun getSavedQuotes(userId: String): Response<List<Quote>> {
        return authApi.getSavedQuotes(userId)
    }

    suspend fun postSavedQuote(quoteId: String): Response<List<Quote>> {
        return authApi.postSavedQuote(quoteId)
    }
}