package com.nesib.yourbooknotes.data.network

import com.nesib.yourbooknotes.models.BookResponse
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.QuotesResponse
import com.nesib.yourbooknotes.models.User
import retrofit2.Response
import retrofit2.http.*

interface MainApi {
    // Quotes
    @GET("/quotes")
    suspend fun getQuotes(): Response<QuotesResponse>

    @POST("/quotes")
    suspend fun postQuote(
        @Body quote: String,
        @Body book: String,
        @Body genre: String
    ): Response<Quote>

    @PATCH("/quotes/{quoteId}")
    suspend fun likeOrDislikeQuote(@Path("quoteId") quoteId: String)

    @PUT("/quotes/{quoteId}")
    suspend fun updateQuote(@Path("quoteId") quoteId: String): Response<Quote>

    @DELETE("/quotes/{quoteId}")
    suspend fun deleteQuote(@Path("quoteId") quoteId: String)

    // Books
    @GET("/books/{bookId}")
    suspend fun getBook(@Path("bookId") bookId: String): Response<BookResponse>

    @GET("/books/{bookToFollowId}")
    suspend fun followOrUnfollowBook(@Path("bookToFollowId") bookId: String): Response<BookResponse>


}