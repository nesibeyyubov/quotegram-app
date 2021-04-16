package com.nesib.yourbooknotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nesib.yourbooknotes.models.Notification
import com.nesib.yourbooknotes.models.NotificationEntity

@Database(entities = [NotificationEntity::class],version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao():NotificationDao
}