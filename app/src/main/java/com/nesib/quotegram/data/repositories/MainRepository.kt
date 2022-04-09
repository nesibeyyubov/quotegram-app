package com.nesib.quotegram.data.repositories

import com.nesib.quotegram.data.local.SharedPreferencesRepository
import com.nesib.quotegram.data.network.MainApi
import com.nesib.quotegram.models.QuotesResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val mainApi: MainApi
) {
    suspend fun getQuotes(page: Int): Response<QuotesResponse> {
        val genres = sharedPreferencesRepository.getFollowingGenres()
        return mainApi.getQuotes(page, genres)
    }

    suspend fun getSingleQuote(quoteId: String) = mainApi.getSingleQuote(quoteId)

    suspend fun reportQuote(reportBody:Map<String,String>) = mainApi.reportQuote(reportBody)

    suspend fun reportUser(reportBody:Map<String,String>) = mainApi.reportUser(reportBody)

    suspend fun reportBook(reportBody:Map<String,String>) = mainApi.reportBook(reportBody)

    suspend fun getQuotesByGenre(genre: String, page: Int) = mainApi.getQuotesByGenre(genre, page)

    suspend fun postQuote(quote: Map<String, String>) =
        mainApi.postQuote(quote)

    suspend fun likeOrDislikeQuote(quoteId: String) = mainApi.likeOrDislikeQuote(quoteId)

    suspend fun updateQuote(quoteId: String, quote: Map<String, String>) =
        mainApi.updateQuote(quoteId, quote)

    suspend fun deleteQuote(quoteId: String) = mainApi.deleteQuote(quoteId)

    suspend fun getNotifications(currentPage:Int=1) = mainApi.getNotifications(currentPage)

    suspend fun clearNotifications() = mainApi.clearNotifications()
}