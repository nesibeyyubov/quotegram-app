package com.nesib.yourbooknotes.models

data class User(
    val username: String?,
    val fullname: String?,
    val email: String?,
    val bio: String?,
    val followers: List<User>?,
    val followingBooks: List<Book>?,
    val followingUsers: List<User>?,
    val followingGenres: List<String>?,
    val quotes: List<Quote>?,
    val savedQuotes: List<Quote>?,
    val profileImage: String?
)
