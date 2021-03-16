package com.nesib.yourbooknotes.data.network

import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.di.MyApplication
import okhttp3.Interceptor
import okhttp3.Response

class MyOkHttpClientInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = SharedPreferencesRepository(MyApplication.context).getUser().token
        val updatedRequest = request.newBuilder()
            .addHeader("Authorization","Bearer $token")
            .build()

        return chain.proceed(updatedRequest)
    }
}