package com.nesib.yourbooknotes.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.repositories.MainRepository
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants
import com.nesib.yourbooknotes.utils.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(application: Application) :AndroidViewModel(application) {
    private val _quotes = MutableLiveData<DataState<QuotesResponse>>()
    val quotes: LiveData<DataState<QuotesResponse>>
        get() = _quotes

    private val _book = MutableLiveData<DataState<BookResponse>>()
    val book: LiveData<DataState<BookResponse>>
        get() = _book

    fun getQuotes() = viewModelScope.launch(Dispatchers.IO) {
        if(_quotes.value==null){
            _quotes.postValue(DataState.Loading())
            val response = MainRepository.getQuotes()
            handleQuoteResponse(response)
        }
    }

    fun getBook(bookId:String) = viewModelScope.launch(Dispatchers.IO) {
        if(_book.value == null){
            _book.postValue(DataState.Loading())
            val response = MainRepository.getBook(bookId)
            handleBookResponse(response)
        }
    }

    private fun handleBookResponse(response: Response<BookResponse>) {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                _book.postValue(DataState.Success(response.body()))
            }
            Constants.CODE_CREATION_SUCCESS ->{

            }
            Constants.CODE_VALIDATION_FAIL -> {

            }
            Constants.CODE_SERVER_ERROR -> {
                _book.postValue(DataState.Fail(message = "Server error"))
            }
            Constants.CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _book.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

    private fun handleQuoteResponse(response: Response<QuotesResponse>) {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                _quotes.postValue(DataState.Success(response.body()))
            }
            Constants.CODE_CREATION_SUCCESS ->{

            }
            Constants.CODE_VALIDATION_FAIL -> {

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
}