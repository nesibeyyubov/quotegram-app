package com.nesib.yourbooknotes.data.repositories

import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.QuotesResponse
import com.nesib.yourbooknotes.models.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MainRepository {
    private val retrofit =
        Retrofit.Builder().baseUrl("http://10.0.2.2:4000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val mainApi = retrofit.create(MainApi::class.java)

    suspend fun getQuotes() = mainApi.getQuotes()

    suspend fun postQuote(quote: String, book: String, genre: String) =
        mainApi.postQuote(quote, book, genre)

    suspend fun likeOrDislikeQuote(quoteId: String) = mainApi.likeOrDislikeQuote(quoteId)

    suspend fun updateQuote(quoteId: String) = mainApi.updateQuote(quoteId)

    suspend fun deleteQuote(quoteId: String) = mainApi.deleteQuote(quoteId)

    suspend fun getBook(bookId: String) = mainApi.getBook(bookId)

    suspend fun getBooks(searchText:String) = mainApi.getBooks(searchText)

}