package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Quote(
    @SerializedName("_id") val id:String,
    val quote: String?,
    val book: Book?,
    val genre: String?,
    val creator: User?,
    val likes: List<String>?,
):Serializable