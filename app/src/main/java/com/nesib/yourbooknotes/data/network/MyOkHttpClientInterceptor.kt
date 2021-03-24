package com.nesib.yourbooknotes.data.network

import android.content.Context
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.di.MyApplication
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MyOkHttpClientInterceptor @Inject constructor(val sharedPreferencesRepository: SharedPreferencesRepository) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sharedPreferencesRepository.getCurrentUser()?.token
        val updatedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer ${token ?: ""}")
            .build()

        return chain.proceed(updatedRequest)
    }
}