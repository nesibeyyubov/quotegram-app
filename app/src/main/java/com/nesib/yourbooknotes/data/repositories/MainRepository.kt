package com.nesib.yourbooknotes.data.repositories

import android.media.session.MediaSession
import android.util.Log
import com.ihsanbal.logging.LoggingInterceptor
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.network.MainApi
import com.nesib.yourbooknotes.data.network.MyOkHttpClientInterceptor
import com.nesib.yourbooknotes.di.MyApplication
import com.nesib.yourbooknotes.models.BookResponse
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.QuotesResponse
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.utils.Constants.API_URL
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.Part
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val mainApi: MainApi
) {
    suspend fun getQuotes(page: Int): Response<QuotesResponse> {
        val userId = sharedPreferencesRepository.getUser().userId
        val genres = if (userId != null) null else sharedPreferencesRepository.getFollowingGenres()
        return mainApi.getQuotes(page, genres, userId)
    }

    suspend fun postQuote(quote: Map<String, String>) =
        mainApi.postQuote(quote)

    suspend fun likeOrDislikeQuote(quoteId: String) = mainApi.likeOrDislikeQuote(quoteId)

    suspend fun updateQuote(quoteId: String,quote: Map<String,String>) = mainApi.updateQuote(quoteId,quote)

    suspend fun deleteQuote(quoteId: String) = mainApi.deleteQuote(quoteId)

    suspend fun getBook(bookId: String) = mainApi.getBook(bookId)

    suspend fun getMoreBookQuotes(bookId: String, page: Int) =
        mainApi.getMoreBookQuotes(bookId, page)

    suspend fun getBooks(searchText: String? = null, page: Int) = mainApi.getBooks(searchText, page)

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

    suspend fun toggleBookFollow(bookId:String) = mainApi.followOrUnfollowBook(bookId)
}