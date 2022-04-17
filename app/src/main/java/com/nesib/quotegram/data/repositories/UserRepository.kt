package com.nesib.quotegram.data.repositories

import com.nesib.quotegram.data.network.AuthApi
import com.nesib.quotegram.models.*
import com.nesib.quotegram.utils.Constants.KEY_EMAIL
import com.nesib.quotegram.utils.Constants.KEY_FULL_NAME
import com.nesib.quotegram.utils.Constants.KEY_PASSWORD
import com.nesib.quotegram.utils.Constants.KEY_PROFILE_IMAGE
import com.nesib.quotegram.utils.Constants.KEY_USERNAME
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(val authApi: AuthApi) {

    suspend fun signup(
        username: String,
        password: String
    ): Response<UserAuth> {
        val map = mapOf(
            KEY_PASSWORD to password,
            KEY_USERNAME to username,
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
            KEY_EMAIL to email,
            KEY_FULL_NAME to fullname,
            KEY_USERNAME to username,
            KEY_PROFILE_IMAGE to profileImage
        )
        return authApi.signupWithGoogle(map)
    }

    suspend fun signInWithGoogle(email: String, profileImage: String): Response<UserAuth> {
        val map = mapOf(KEY_EMAIL to email, KEY_PROFILE_IMAGE to profileImage)
        return authApi.signInWithGoogle(map)
    }

    suspend fun login(loginBody: Map<String, String>) = authApi.login(loginBody)

    suspend fun followOrUnFollowUser(userId: String) = authApi.followOrUnFollowUser(userId)

    suspend fun saveFollowingGenres(genres: String) =
        authApi.saveFollowingGenres(genres)

    suspend fun updateUser(body: Map<String, String>) = authApi.updateUser(body)

    suspend fun getUser(userId: String) = authApi.getUser(userId)

    suspend fun getMoreUserQuotes(userId: String, page: Int) =
        authApi.getMoreUserQuotes(userId, page)

    suspend fun getUsers(searchQuery: String, currentPage: Int) =
        authApi.getUsers(searchQuery, currentPage)

}