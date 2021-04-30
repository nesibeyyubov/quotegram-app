package com.nesib.quotegram.data.network

import com.nesib.quotegram.data.local.SharedPreferencesRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MyOkHttpClientInterceptor @Inject constructor(val sharedPreferencesRepository: SharedPreferencesRepository) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sharedPreferencesRepository.getToken()
        val updatedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer ${token ?: ""}")
            .build()

        return chain.proceed(updatedRequest)
    }
}