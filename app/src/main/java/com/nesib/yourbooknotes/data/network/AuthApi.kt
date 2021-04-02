package com.nesib.yourbooknotes.data.network

import com.nesib.yourbooknotes.models.*
import retrofit2.Response
import retrofit2.http.*


interface AuthApi {
    // Authentication
    @POST("/auth/login/")
    suspend fun login(@Body loginBody: Map<String, String>): Response<AuthResponse>

    @POST("/auth/signup")
    suspend fun signup(@Body signupBody: Map<String, String>): Response<AuthResponse>

    @POST("/auth/google-login")
    suspend fun signInWithGoogle(@Body loginBody: Map<String, String>): Response<AuthResponse>

    @POST("/auth/google-signup")
    suspend fun signupWithGoogle(@Body signupBody: Map<String, String>): Response<AuthResponse>

    // Users
    @GET("/users/")
    suspend fun getUsers(@Query("search") searchQuery: String): Response<UsersResponse>

    @GET("/users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserResponse>

    @GET("/users/{userId}")
    suspend fun getMoreUserQuotes(
        @Path("userId") userId: String,
        @Query("page") page: Int
    ): Response<QuotesResponse>

    @GET("/users/{userId}/savedQuotes")
    suspend fun getSavedQuotes(@Path("userId") userId: String): Response<List<Quote>>

    @POST("/users/{quoteId}/savedQuotes")
    suspend fun postSavedQuote(@Body quoteId: String): Response<List<Quote>>

    @POST("/users/{userId}/followers")
    suspend fun followOrUnFollowUser(@Path("userId") userId: String):Response<UserResponse>

    @PUT("/users/")
    suspend fun updateUser(
        @Body username: String?,
        @Body fullname: String?,
        @Body email: String?,
        @Body password: String?,
        @Body bio: String?,
    ): Response<User>

    @PATCH("/users/")
    suspend fun saveFollowingGenres(
        @Query("genres") genres: String,
        @Header("Authorization") authHeader: String
    ): Response<UserResponse>

}