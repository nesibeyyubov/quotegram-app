package com.nesib.yourbooknotes.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocalModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context:Context):SharedPreferences{
        Log.d("mytag", "provideSharedPreferences: creating shared prefs...")
        val masterKey =MasterKey.Builder(context).setKeyGenParameterSpec(MasterKeys.AES256_GCM_SPEC).build()
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "user",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        Log.d("mytag", "provideSharedPreferences: finished shared prefs !")

        return sharedPreferences
    }
}