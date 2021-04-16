package com.nesib.yourbooknotes.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nesib.yourbooknotes.models.Notification
import com.nesib.yourbooknotes.models.NotificationEntity

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications_table")
    suspend fun getNotifications(): List<NotificationEntity>
}