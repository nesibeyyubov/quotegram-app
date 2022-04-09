package com.nesib.quotegram.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Quote(
    @SerializedName("_id") val id: String,
    var quote: String?,
    var genre: String?,
    var creator: User?,
    var likes: List<String>?,
    var liked: Boolean = false,
    var saved: Boolean = false
) : Serializable