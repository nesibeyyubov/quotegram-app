package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("_id") val id:String,
    val name: String?,
    val genre: String?,
    val author: String?,
    val image: String?,
    val creator: String?,
    val quotes: List<Quote>?,
    val followers: List<String>?
)
