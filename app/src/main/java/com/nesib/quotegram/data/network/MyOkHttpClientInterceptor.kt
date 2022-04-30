package com.nesib.quotegram.data.network

import com.nesib.quotegram.data.local.SharedPreferencesRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MyOkHttpClientInterceptor @Inject constructor(val sharedPreferencesRepository: SharedPreferencesRepository) :
    Interceptor {
    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sharedPreferencesRepository.getToken()
        val updatedRequest = request.newBuilder()
            .addHeader(HEADER_AUTHORIZATION, "$BEARER ${token ?: ""}")
            .build()

        return chain.proceed(updatedRequest)
    }
}