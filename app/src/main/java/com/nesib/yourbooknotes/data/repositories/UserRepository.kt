package com.nesib.yourbooknotes.data.repositories

import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants.API_URL
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(val authApi: AuthApi) {

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

    suspend fun saveFollowingGenres(genres: String,authHeader:String) = authApi.saveFollowingGenres(genres,authHeader)

    suspend fun updateUser(
        username: String?,
        fullname: String?,
        email: String?,
        password: String?,
        bio: String?
    ): Response<User> {
        return authApi.updateUser(username, fullname, email, password, bio)
    }

    suspend fun getUser(userId: String) = authApi.getUser(userId)

    suspend fun getMoreUserQuotes(userId:String,page:Int) = authApi.getMoreUserQuotes(userId,page)

    suspend fun getUsers(searchQuery:String) = authApi.getUsers(searchQuery)

    suspend fun getSavedQuotes(userId: String) = authApi.getSavedQuotes(userId)

    suspend fun postSavedQuote(quoteId: String) = authApi.postSavedQuote(quoteId)
}