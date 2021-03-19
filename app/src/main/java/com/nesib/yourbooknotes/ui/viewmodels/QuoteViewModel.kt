package com.nesib.yourbooknotes.ui.viewmodels

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


    fun getQuotes(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (_quotes.value == null) {
            _quotes.postValue(DataState.Loading())
            val response = mainRepository.getQuotes(page)
            handleQuotesResponse(response)
        }
    }

    fun getMoreQuotes(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        _quotes.postValue(DataState.Loading())
        val response = mainRepository.getQuotes(page)
        handleQuotesResponse(response)
    }

    fun postQuote(quote: Map<String, String>) = viewModelScope.launch(Dispatchers.IO) {
        _quote.postValue(DataState.Loading())
        val response = mainRepository.postQuote(quote)
        handleQuoteResponse(response)
    }

    fun toggleLike(quoteId:String) = viewModelScope.launch(Dispatchers.IO) {
        _quote.postValue(DataState.Loading())
        val response = mainRepository.likeOrDislikeQuote(quoteId)
        handleQuoteResponse(response)
    }

    private fun handleQuotesResponse(response: Response<QuotesResponse>) {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                // Check when there is not any quote
                if (quoteList.isEmpty()) {
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

    private fun handleQuoteResponse(response: Response<QuoteResponse>) {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                _quote.postValue(DataState.Success(response.body()))
            }
            Constants.CODE_CREATION_SUCCESS -> {
                Log.d("mytag", "handleQuoteResponse: response.body=${response.body()}")
                _quote.postValue(DataState.Success(response.body()))
            }
            Constants.CODE_VALIDATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _quote.postValue(DataState.Fail(message = authFailResponse.message))
            }
            Constants.CODE_SERVER_ERROR -> {
                _quote.postValue(DataState.Fail(message = "Server error"))
            }
            Constants.CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _quote.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }
}