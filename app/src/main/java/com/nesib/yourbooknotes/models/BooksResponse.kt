package com.nesib.yourbooknotes.models

data class BooksResponse(
    val message:String? = null,
    val books:List<Book>
)
