package com.nesib.yourbooknotes.data.repositories

import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MainRepository {
    private val retrofit =
        Retrofit.Builder().baseUrl("url").addConverterFactory(GsonConverterFactory.create())
            .build()
    private val mainApi = retrofit.create(MainApi::class.java)


    suspend fun getQuotes(): Response<List<Quote>> {
        return mainApi.getQuotes()
    }
    suspend fun postQuote(quote:String,book:String,genre:String,): Response<Quote> {
        return mainApi.postQuote(quote,book,genre)
    }
    suspend fun likeOrDislikeQuote(quoteId: String) {
        return mainApi.likeOrDislikeQuote(quoteId)
    }
    suspend fun updateQuote(quoteId: String): Response<Quote> {
        return mainApi.updateQuote(quoteId)
    }
    suspend fun deleteQuote(quoteId: String) {
        return mainApi.deleteQuote(quoteId)
    }
}