package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("_id") val id: String?,
    val likerUserId: String?,
    val likedQuoteId: String?,
    val userId: String?,
    val username: String?,
    val userPhoto: String?,
    val read:Boolean = false
)
