package com.nesib.yourbooknotes.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.repositories.MainRepository
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants.CODE_AUTHENTICATION_FAIL
import com.nesib.yourbooknotes.utils.Constants.CODE_CREATION_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_SERVER_ERROR
import com.nesib.yourbooknotes.utils.Constants.CODE_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.yourbooknotes.utils.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var quoteList = mutableListOf<Quote>()
    private var bookList = mutableListOf<Book>()
    private val _quotes = MutableLiveData<DataState<QuotesResponse>>()
    val quotes: LiveData<DataState<QuotesResponse>>
        get() = _quotes

    private val _quote = MutableLiveData<DataState<QuoteResponse>>()
    val quote: LiveData<DataState<QuoteResponse>>
        get() = _quote

    private val _book = MutableLiveData<DataState<BookResponse>>()
    val book: LiveData<DataState<BookResponse>>
        get() = _book

    private val _books = MutableLiveData<DataState<BooksResponse>>()
    val books: LiveData<DataState<BooksResponse>>
        get() = _books


    fun getQuotes(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (_quotes.value == null) {
            _quotes.postValue(DataState.Loading())
            val response = MainRepository.getQuotes(page)
            handleQuotesResponse(response)
        }
    }

    fun getMoreQuotes(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        _quotes.postValue(DataState.Loading())
        val response = MainRepository.getQuotes(page)
        handleQuotesResponse(response)
    }

    fun postQuote(quote: Map<String, String>) = viewModelScope.launch(Dispatchers.IO) {
        _quote.postValue(DataState.Loading())
        val response = MainRepository.postQuote(quote)
        handleQuoteResponse(response)
    }

    fun getBook(bookId: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (_book.value == null) {
            _book.postValue(DataState.Loading())
            val response = MainRepository.getBook(bookId)
            handleBookResponse(response)
        }
    }

    fun getMoreBookQuotes(bookId: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        _quotes.postValue(DataState.Loading())
        val response = MainRepository.getMoreBookQuotes(bookId, page)
        handleQuotesResponse(response)
    }

    fun getBooks(searchText: String?=null,page:Int = 1,searchTextChanged:Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        _books.postValue(DataState.Loading())
        val response = MainRepository.getBooks(searchText,page)
        handleBooksResponse(response,searchTextChanged)
    }

    fun discoverBooks(searchText: String ) = viewModelScope.launch(Dispatchers.IO) {
        _books.postValue(DataState.Loading())
        val response = MainRepository.getBooks(searchText,1)
        handleBooksResponse(response,false)
    }

    fun postBook(name:String,author:String,genre:String,image:File) = viewModelScope.launch(Dispatchers.IO) {
        _book.postValue(DataState.Loading())
        val response = MainRepository.postBook(name,author,genre,image)
        handleBookResponse(response)
    }

    private fun handleBookResponse(response: Response<BookResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                quoteList = response.body()!!.book!!.quotes!!.toMutableList()
                _book.postValue(DataState.Success(response.body()))
            }
            CODE_CREATION_SUCCESS -> {
                _book.postValue(DataState.Success())
            }
            CODE_VALIDATION_FAIL -> {
                val validationFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _book.postValue(DataState.Fail(message = validationFailResponse.message))
            }
            CODE_SERVER_ERROR -> {
                _book.postValue(DataState.Fail(message = "Server error"))
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _book.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

    private fun handleBooksResponse(response: Response<BooksResponse>,searchTextChanged: Boolean) {
        when (response.code()) {
            CODE_SUCCESS -> {
                if(bookList.isEmpty() || searchTextChanged){
                    bookList = response.body()?.books!!.toMutableList()
                }else{
                    response.body()?.books!!.forEach { book->bookList.add(book) }
                }
                _books.postValue(DataState.Success(BooksResponse(books = bookList.toList())))
            }
            CODE_SERVER_ERROR -> {
                _books.postValue(DataState.Fail(message = "Server error"))
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _books.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

    private fun handleQuotesResponse(response: Response<QuotesResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                // Check when there is not any quote
                if (quoteList.isEmpty()) {
                    quoteList = response.body()!!.quotes.toMutableList()
                } else {
                    response.body()!!.quotes.forEach { quote -> quoteList.add(quote) }
                }
                _quotes.postValue(DataState.Success(QuotesResponse(quoteList.toList())))

            }
            CODE_SERVER_ERROR -> {
                _quotes.postValue(DataState.Fail(message = "Server error"))
            }
            CODE_AUTHENTICATION_FAIL -> {
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
            CODE_SUCCESS -> {
                _quote.postValue(DataState.Success(response.body()))
            }
            CODE_CREATION_SUCCESS -> {
                Log.d("mytag", "handleQuoteResponse: response.body=${response.body()}")
                _quote.postValue(DataState.Success(response.body()))
            }
            CODE_VALIDATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _quote.postValue(DataState.Fail(message = authFailResponse.message))
            }
            CODE_SERVER_ERROR -> {
                _quote.postValue(DataState.Fail(message = "Server error"))
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _quote.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }
}