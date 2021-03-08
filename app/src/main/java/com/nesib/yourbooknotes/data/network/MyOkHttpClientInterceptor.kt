package com.nesib.yourbooknotes.data.network

import okhttp3.Interceptor
import okhttp3.Response

class MyOkHttpClientInterceptor(val token:String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val updatedRequest = request.newBuilder()
            .addHeader("Authorization","Bearer $token")
            .build()

        return chain.proceed(updatedRequest)
    }
}