package com.nesib.quotegram.data.network

import com.nesib.quotegram.models.*
import retrofit2.Response
import retrofit2.http.*


interface AuthApi {
    @POST("/auth/google-signup")
    suspend fun authorizeWithGoogle(@Body signupBody: Map<String, String>): Response<UserAuth>

    // Users
    @GET("/users/")
    suspend fun getUsers(
        @Query("search") searchQuery: String,
        @Query("page") currentPage: Int
    ): Response<UsersResponse>

    @GET("/users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserResponse>

    @GET("/users/{userId}")
    suspend fun getMoreUserQuotes(
        @Path("userId") userId: String,
        @Query("page") page: Int
    ): Response<QuotesResponse>

    @POST("/users/{userId}/followers")
    suspend fun followOrUnFollowUser(@Path("userId") userId: String): Response<UserResponse>

    @PUT("/users/")
    suspend fun updateUser(
        @Body body: Map<String, String>
    ): Response<UserResponse>

    @PATCH("/users/")
    suspend fun saveFollowingGenres(
        @Query("genres") genres: String,
    ): Response<UserAuth>

}