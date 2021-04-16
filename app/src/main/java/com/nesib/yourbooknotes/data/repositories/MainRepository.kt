package com.nesib.yourbooknotes.data.repositories

import android.util.Log
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.models.BookResponse
import com.nesib.yourbooknotes.models.QuotesResponse
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

    suspend fun getBook(bookId: String) = mainApi.getBook(bookId)

    suspend fun getMoreBookQuotes(bookId: String, page: Int) =
        mainApi.getMoreBookQuotes(bookId, page)

    suspend fun getBooks(searchText: String? = null, page: Int) = mainApi.getBooks(searchText, page)

    suspend fun discoverBooks(genre: String,page:Int,search:String) = mainApi.discoverBooks(genre,page,search)

    suspend fun postBook(
        name: String,
        author: String,
        genre: String,
        image: File
    ): Response<BookResponse> {
        val imageMimeType = "image/${image.name.split(".")[1]}"
        val imageRequestBody = image.asRequestBody(imageMimeType.toMediaTypeOrNull())
        val imageBodyPart = MultipartBody.Part.createFormData("image", image.name, imageRequestBody)

        val nameRequestBody = name.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val authorRequestBody = author.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val genreRequestBody = genre.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        Log.d("mytag", "postBook: ${imageBodyPart.body}")

        return mainApi.postBook(nameRequestBody, authorRequestBody, genreRequestBody, imageBodyPart)
    }

    suspend fun toggleBookFollow(bookId: String) = mainApi.followOrUnfollowBook(bookId)

    suspend fun getNotifications() = mainApi.getNotifications()

    suspend fun readNotifications() = mainApi.readNotifications()
}