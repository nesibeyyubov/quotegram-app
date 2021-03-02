package com.nesib.yourbooknotes.models

data class Quote(
    val quote: String?,
    val book: Book?,
    val genre: String?,
    val creator: User?,
    val likes: List<User>?,
)