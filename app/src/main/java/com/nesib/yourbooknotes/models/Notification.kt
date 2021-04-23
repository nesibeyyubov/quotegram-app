package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("_id") val id: String?,
    val likerUserId: String?,
    val likedQuoteId: String?,
    val userId: String?,
    val userPhoto: String?,
    val bookName: String?,
    val likeCount:Int,
    val read: Boolean = false
)
