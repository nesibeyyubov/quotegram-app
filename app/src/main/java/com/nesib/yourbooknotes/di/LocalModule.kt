package com.nesib.yourbooknotes.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.nesib.yourbooknotes.data.local.AppDatabase
import com.nesib.yourbooknotes.data.local.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocalModule {

    @Provides
    @Named("themeSharedPreferences")
    fun provideSharedPreferences(@ApplicationContext context: Context):SharedPreferences{
        val sharedPreferences = context.getSharedPreferences("theme",Context.MODE_PRIVATE)
        return sharedPreferences
    }

    @Provides
    @Singleton
    @Named(("encryptedSharedPreferences"))
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey =
            MasterKey.Builder(context).setKeyGenParameterSpec(MasterKeys.AES256_GCM_SPEC).build()
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "user",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences
    }

    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "notifications_db").build()
    }

    @Provides
    fun provideNotificationDao(appDatabase: AppDatabase):NotificationDao{
        return appDatabase.notificationDao()
    }
}