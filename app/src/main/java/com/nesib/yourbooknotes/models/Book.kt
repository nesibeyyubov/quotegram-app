package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Book(
    @SerializedName("_id") val id: String,
    val name: String?,
    val genre: String?,
    val author: String?,
    val image: String?,
    val creator: String?,
    val quotes: List<Quote>?,
    var followers: List<String>?,
    val totalQuoteCount: Int?
) : Serializable
