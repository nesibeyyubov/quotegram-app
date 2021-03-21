package com.nesib.yourbooknotes.ui.viewmodels

import android.app.AlertDialog
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.repositories.MainRepository
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants
import com.nesib.yourbooknotes.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    private var quoteList = mutableListOf<Quote>()
    private val _quotes = MutableLiveData<DataState<QuotesResponse>>()
    val quotes: LiveData<DataState<QuotesResponse>>
        get() = _quotes

    private val _quote = MutableLiveData<DataState<QuoteResponse>>()
    val quote: LiveData<DataState<QuoteResponse>>
        get() = _quote

    private val _likeQuote = MutableLiveData<DataState<QuoteResponse>>()
    val likeQuote: LiveData<DataState<QuoteResponse>>
        get() = _likeQuote

    private val _deleteQuote = MutableLiveData<DataState<QuoteResponse>>()
    val deleteQuote: LiveData<DataState<QuoteResponse>>
        get() = _deleteQuote


    fun getQuotes(page: Int = 1, forced: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if (_quotes.value == null || forced) {
            _quotes.postValue(DataState.Loading())
            val response = mainRepository.getQuotes(page)
            handleQuotesResponse(response, forced)
        }
    }

    fun deleteQuote(quote: Quote) = viewModelScope.launch(Dispatchers.IO) {
        _deleteQuote.postValue(DataState.Loading())
        val response = mainRepository.deleteQuote(quote.id)
        val handledResponse = handleQuoteResponse(response)
        if(handledResponse is DataState.Success){
            Log.d("mytag", "deleteQuote: quoteList.size: ${quoteList.size}")
            quoteList.remove(quote)
            _quotes.postValue(DataState.Success(data = QuotesResponse(quoteList.toList())))
        }
        _deleteQuote.postValue(handledResponse)
    }
    fun clearDeleteQuote(){
        _deleteQuote.value = null
    }

    fun getMoreQuotes(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        _quotes.postValue(DataState.Loading())
        val response = mainRepository.getQuotes(page)
        handleQuotesResponse(response)
    }

    fun postQuote(quote: Map<String, String>) = viewModelScope.launch(Dispatchers.IO) {
        _quote.postValue(DataState.Loading())
        val response = mainRepository.postQuote(quote)
        val handledResponse = handleQuoteResponse(response)
        _quote.postValue(handledResponse)
    }

    fun toggleLike(quote: Quote) = viewModelScope.launch(Dispatchers.IO) {
        _likeQuote.postValue(DataState.Loading())
        _quotes.value?.data?.quotes?.find { q -> q.id == quote.id }?.likes = quote.likes
        val response = mainRepository.likeOrDislikeQuote(quote.id)
        val handledResponse = handleQuoteResponse(response)
        _likeQuote.postValue(handledResponse)
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
}