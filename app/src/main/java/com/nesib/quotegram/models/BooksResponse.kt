package com.nesib.quotegram.models

data class BooksResponse(
    val message:String? = null,
    val books:List<Book>
)
