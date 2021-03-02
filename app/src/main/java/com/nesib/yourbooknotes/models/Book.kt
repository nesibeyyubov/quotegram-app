package com.nesib.yourbooknotes.models

data class Book(
    val name: String?,
    val genre: String?,
    val author: String?,
    val image: String?,
    val creator: String?,
    val quotes: List<Quote>?,
    val followers: List<User>?
)
