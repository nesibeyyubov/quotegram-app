package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id:String,
    val username: String?,
    val fullname: String?,
    val email: String?,
    val bio: String?,
    val followers: List<String>?,
    val followingBooks: List<String>?,
    val followingUsers: List<String>?,
    val followingGenres: List<String>?,
    val quotes: List<Quote>?,
    val savedQuotes: List<String>?,
    val profileImage: String?
)
