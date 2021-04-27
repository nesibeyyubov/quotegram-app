package com.nesib.yourbooknotes.ui.viewmodels

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.repositories.MainRepository
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants
import com.nesib.yourbooknotes.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    val mainRepository: MainRepository, application: Application,
) : AndroidViewModel(application) {

    private var quoteList = mutableListOf<Quote>()
    private val _quotes = MutableLiveData<DataState<QuotesResponse>>()
    val quotes: LiveData<DataState<QuotesResponse>>
        get() = _quotes

    private val _quotesByGenre = MutableLiveData<DataState<QuotesResponse>>()
    val quotesByGenre: LiveData<DataState<QuotesResponse>>
        get() = _quotesByGenre

    private val _quote = MutableLiveData<DataState<QuoteResponse>>()
    val quote: LiveData<DataState<QuoteResponse>>
        get() = _quote

    private val _likeQuote = MutableLiveData<DataState<QuoteResponse>>()
    val likeQuote: LiveData<DataState<QuoteResponse>>
        get() = _likeQuote

    private val _updateQuote = MutableLiveData<DataState<QuoteResponse>>()
    val updateQuote: LiveData<DataState<QuoteResponse>>
        get() = _updateQuote

    private val _deleteQuote = MutableLiveData<DataState<QuoteResponse>>()
    val deleteQuote: LiveData<DataState<QuoteResponse>>
        get() = _deleteQuote


    fun getQuotesByGenre(genre: String, page: Int = 1, forced: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection()) {
                try {
                    Log.d("mytag", "getQuotesByGenre: ${genre}")
                    if (_quotes.value == null || page > 1 || forced) {
                        _quotes.postValue(DataState.Loading())
                        val response = mainRepository.getQuotesByGenre(genre, page)
                        handleQuotesResponse(response)
                    }
                } catch (e: Exception) {
                    _quotes.postValue(DataState.Fail())
                }
            } else {
                _quotes.postValue(DataState.Fail(message = "No internet connection"))
            }
        }


    fun getQuotes(page: Int = 1, forced: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                if (_quotes.value == null || forced) {
                    _quotes.postValue(DataState.Loading())
                    val response = mainRepository.getQuotes(page)
                    handleQuotesResponse(response, forced)
                }
            } catch (exception: Exception) {
                _quotes.postValue(DataState.Fail())
            }
        } else {
            _quotes.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun getSingleQuote(quoteId: String) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _quote.postValue(DataState.Loading())
                val response = mainRepository.getSingleQuote(quoteId)
                val handledResponse = handleQuoteResponse(response)
                _quote.postValue(handledResponse)
            } catch (exception: Exception) {
                _quote.postValue(DataState.Fail())
            }
        } else {
            _quote.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun updateQuote(oldQuote: Quote, newQuote: Map<String, String>) =
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection()) {
                try {
                    _updateQuote.postValue(DataState.Loading())
                    val response = mainRepository.updateQuote(oldQuote.id, newQuote)
                    val handledResponse = handleQuoteResponse(response)
                    val index = quoteList.indexOf(oldQuote)
                    if (index != -1) {
                        quoteList.removeAt(index)
                        val newQuoteModel =
                            oldQuote.copy(quote = newQuote["quote"], genre = newQuote["genre"])
                        quoteList.add(index, newQuoteModel)
                    }
                    _quotes.postValue(DataState.Success(QuotesResponse(quoteList.toList())))
                    _updateQuote.postValue(handledResponse)
                } catch (e: Exception) {
                    _updateQuote.postValue(DataState.Fail())
                }
            } else {
                _updateQuote.postValue(DataState.Fail(message = "No internet connection"))
            }

        }

    fun deleteQuote(quote: Quote) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _deleteQuote.postValue(DataState.Loading())
                val response = mainRepository.deleteQuote(quote.id)
                val handledResponse = handleQuoteResponse(response)
                if (handledResponse is DataState.Success) {
                    quoteList.remove(quote)
                    _quotes.postValue(DataState.Success(data = QuotesResponse(quoteList.toList())))
                }
                _deleteQuote.postValue(handledResponse)
            } catch (e: Exception) {
                _deleteQuote.postValue(DataState.Fail())
            }
        } else {
            _deleteQuote.postValue(DataState.Fail(message = "No internet connection"))
        }
    }


    fun getMoreQuotes(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _quotes.postValue(DataState.Loading())
                val response = mainRepository.getQuotes(page)
                handleQuotesResponse(response)
            } catch (e: Exception) {

                _quotes.postValue(DataState.Fail())
            }
        } else {
            _quotes.postValue(DataState.Fail(message = "No internet connection"))
        }
    }

    fun postQuote(quote: Map<String, String>) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _quote.postValue(DataState.Loading())
                val response = mainRepository.postQuote(quote)
                val handledResponse = handleQuoteResponse(response)
                _quote.postValue(handledResponse)
            } catch (e: Exception) {
                _quote.postValue(DataState.Fail())
            }
        } else {
            _quote.postValue(DataState.Fail(message = "No internet connection"))
        }
    }

    fun toggleLike(quote: Quote) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _likeQuote.postValue(DataState.Loading())
                _quotes.value?.data?.quotes?.find { q -> q.id == quote.id }?.let {
                    it.likes = quote.likes
                    it.liked = quote.liked
                }
                val bookName = quote.book?.name ?: "Unknown"
                val bookBody = mapOf("bookName" to bookName)
                val response = mainRepository.likeOrDislikeQuote(quote.id, bookBody)
                val handledResponse = handleQuoteResponse(response)
                _likeQuote.postValue(handledResponse)
            } catch (e: Exception) {
                _likeQuote.postValue(DataState.Fail())
            }
        } else {
            _likeQuote.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    private fun handleQuotesResponse(response: Response<QuotesResponse>, forced: Boolean = false) {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                // Check when there is not any quote
                if (quoteList.isEmpty() || forced) {
                    quoteList = response.body()!!.quotes.toMutableList()
                } else {
                    response.body()!!.quotes.forEach { quote -> quoteList.add(quote) }
                }
                _quotes.postValue(DataState.Success(QuotesResponse(quoteList.toList())))

            }
            Constants.CODE_SERVER_ERROR -> {
                _quotes.postValue(DataState.Fail(message = "Server error"))
            }
            Constants.CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _quotes.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

    private fun handleQuoteResponse(response: Response<QuoteResponse>): DataState<QuoteResponse> {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                Log.d("mytag", "handleQuoteResponse: SUCCESS")
                return DataState.Success(response.body())
            }
            Constants.CODE_CREATION_SUCCESS -> {
                return DataState.Success(response.body())
            }
            Constants.CODE_VALIDATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                return DataState.Fail(message = authFailResponse.message)
            }
            Constants.CODE_SERVER_ERROR -> {
                return DataState.Fail(message = "Server error")
            }
            Constants.CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                return DataState.Fail(message = authFailResponse.message)
            }
        }
        return DataState.Fail(message = "No error code provided")
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun clearLiveDataValues() {
        _deleteQuote.value = null
        _updateQuote.value = null
        _quote.value = null
    }
}