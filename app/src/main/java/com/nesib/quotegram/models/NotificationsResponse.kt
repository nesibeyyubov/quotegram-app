package com.nesib.quotegram.models

data class NotificationsResponse(
    val message: String?=null,
    val notifications: List<Notification>?
)
