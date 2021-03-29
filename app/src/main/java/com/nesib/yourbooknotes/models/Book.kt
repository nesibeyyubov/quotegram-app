package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Book(
    @SerializedName("_id") val id: String,
    val name: String? = null,
    val genre: String? = null,
    val author: String? = null,
    val image: String? = null,
    val creator: String? = null,
    val quotes: List<Quote>? = null,
    var followers: List<String>? = null,
    val totalQuoteCount: Int? = null,
    var following:Boolean = false,
    var followerCount:Int = 0
) : Serializable
