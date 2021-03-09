package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Quote(
    @SerializedName("_id") val id:String,
    var quote: String?,
    var book: Book?,
    var genre: String?,
    var creator: User?,
    var likes: List<String>?,
):Serializable