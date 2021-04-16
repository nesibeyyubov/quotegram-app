package com.nesib.yourbooknotes.models

import com.google.gson.annotations.SerializedName

data class NotificationsResponse(
    val message: String?=null,
    val notifications: List<Notification>?
)
