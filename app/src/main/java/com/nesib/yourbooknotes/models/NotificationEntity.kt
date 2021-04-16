package com.nesib.yourbooknotes.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "notifications_table")
data class NotificationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "liker_user_id") val likerUserId: String,
    @ColumnInfo(name = "liker_quote_id") val likedQuoteId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "user_photo") val userPhoto: String,
    @ColumnInfo(name = "read") val read: Boolean = false
)
