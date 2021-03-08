package com.nesib.yourbooknotes.data.network

import com.nesib.yourbooknotes.models.*
import retrofit2.Response
import retrofit2.http.*

interface MainApi {
    // Quotes
    @GET("/quotes")
    suspend fun getQuotes(): Response<QuotesResponse>

    @POST("/quotes")
    suspend fun postQuote(
        @Body quote: Map<String, String>,
    ): Response<QuoteResponse>

    @PATCH("/quotes/{quoteId}")
    suspend fun likeOrDislikeQuote(@Path("quoteId") quoteId: String)

    @PUT("/quotes/{quoteId}")
    suspend fun updateQuote(@Path("quoteId") quoteId: String): Response<QuotesResponse>

    @DELETE("/quotes/{quoteId}")
    suspend fun deleteQuote(@Path("quoteId") quoteId: String)

    // Books
    @GET("/books/")
    suspend fun getBooks(@Query("search") searchText: String): Response<BooksResponse>

    @GET("/books/{bookId}")
    suspend fun getBook(@Path("bookId") bookId: String): Response<BookResponse>

    @GET("/books/{bookToFollowId}")
    suspend fun followOrUnfollowBook(@Path("bookToFollowId") bookId: String): Response<BookResponse>


}