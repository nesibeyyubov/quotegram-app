package com.nesib.yourbooknotes.data.repositories

import android.media.session.MediaSession
import android.util.Log
import com.ihsanbal.logging.LoggingInterceptor
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.data.network.MyOkHttpClientInterceptor
import com.nesib.yourbooknotes.di.MyApplication
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.QuotesResponse
import com.nesib.yourbooknotes.models.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MainRepository {
    private var sharedPreferencesRepository:SharedPreferencesRepository
    private var retrofit: Retrofit
    init {
        sharedPreferencesRepository = SharedPreferencesRepository(MyApplication.context)
        val token = sharedPreferencesRepository.getUser().token
        val loggingInterceptor = LoggingInterceptor.Builder().build()
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(MyOkHttpClientInterceptor(token))
            .addInterceptor(loggingInterceptor)
            .build()
        retrofit =
            Retrofit.Builder().baseUrl("http://10.0.2.2:4000")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
    }

    private val mainApi = retrofit.create(MainApi::class.java)

    suspend fun getQuotes() = mainApi.getQuotes()

    suspend fun postQuote(quote:Map<String,String>) =
        mainApi.postQuote(quote)

    suspend fun likeOrDislikeQuote(quoteId: String) = mainApi.likeOrDislikeQuote(quoteId)

    suspend fun updateQuote(quoteId: String) = mainApi.updateQuote(quoteId)

    suspend fun deleteQuote(quoteId: String) = mainApi.deleteQuote(quoteId)

    suspend fun getBook(bookId: String) = mainApi.getBook(bookId)

    suspend fun getBooks(searchText:String) = mainApi.getBooks(searchText)

}