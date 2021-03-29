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
class BookViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
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

    private val _bookFollow = MutableLiveData<DataState<BookResponse>>()
    val bookFollow: LiveData<DataState<BookResponse>>
        get() = _bookFollow

    private val _books = MutableLiveData<DataState<BooksResponse>>()
    val books: LiveData<DataState<BooksResponse>>
        get() = _books


    fun getBook(bookId: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (_book.value == null) {
            _book.postValue(DataState.Loading())
            val response = mainRepository.getBook(bookId)
            val handledResponse = handleBookResponse(response)
            _book.postValue(handledResponse)
        }
    }

    fun notifyQuoteRemoved(quote:Quote)= viewModelScope.launch(Dispatchers.Default){
        quoteList.remove(quote)
        _quotes.postValue(DataState.Success(data = QuotesResponse(quoteList.toList())))
    }
    fun notifyQuoteUpdated(quote:Quote) = viewModelScope.launch(Dispatchers.Default) {
        val quoteToDelete = quoteList.find{q -> q.id == quote.id}
        val index = quoteList.indexOf(quoteToDelete)
        Log.d("mytag", "index: $index")
        if(index != -1){
            quoteList.remove(quoteToDelete)
            quoteList.add(index,quote)
            Log.d("mytag", "quote updated !")
        }
        _quotes.postValue(DataState.Success(QuotesResponse(quoteList.toList())))
    }

    fun getMoreBookQuotes(bookId: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        _quotes.postValue(DataState.Loading())
        val response = mainRepository.getMoreBookQuotes(bookId, page)
        handleQuotesResponse(response)
    }

    fun getBooks(searchText: String? = null, page: Int = 1, notPaginated: Boolean = true) =
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            _books.postValue(DataState.Loading())
            val response = mainRepository.getBooks(searchText, page)
            handleBooksResponse(response, notPaginated)
        }

    fun discoverBooks(genre: String="all", page: Int = 1,notPaginated: Boolean = true,searchText:String="") = viewModelScope.launch(Dispatchers.IO) {
            _books.postValue(DataState.Loading())
            val response = mainRepository.discoverBooks(genre, page, searchText)
            handleBooksResponse(response,notPaginated)
    }
//
//    fun discoverBooks(searchText: String) = viewModelScope.launch(Dispatchers.IO) {
//        _books.postValue(DataState.Loading())
//        val response = mainRepository.getBooks(searchText, 1)
//        handleBooksResponse(response, false)
//    }

    fun postBook(name: String, author: String, genre: String, image: File) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        _book.postValue(DataState.Loading())
        val response = mainRepository.postBook(name, author, genre, image)
        val handledResponse = handleBookResponse(response)
        _book.postValue(handledResponse)
    }

    fun toggleBookFollow(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        _bookFollow.postValue(DataState.Loading())
        val response = mainRepository.toggleBookFollow(book.id)
        val handledResponse = handleBookResponse(response)
        _book.value?.data?.book?.followers = book.followers
        _book.value?.data?.book?.following = book.following
        _bookFollow.postValue(handledResponse)
    }




    private fun handleBookResponse(response: Response<BookResponse>):DataState<BookResponse> {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                response.body()?.book?.let {
                    quoteList = it.quotes!!.toMutableList()
                }
                return DataState.Success(response.body())
            }
            Constants.CODE_CREATION_SUCCESS -> {
                return DataState.Success()
            }
            Constants.CODE_VALIDATION_FAIL -> {
                val validationFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                return DataState.Fail(message = validationFailResponse.message)
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

    private fun handleBooksResponse(response: Response<BooksResponse>, notPaginated: Boolean) {
        when (response.code()) {
            Constants.CODE_SUCCESS -> {
                if (bookList.isEmpty() || notPaginated) {
                    bookList = response.body()?.books!!.toMutableList()
                } else {
                    response.body()?.books!!.forEach { book -> bookList.add(book) }
                }
                _books.postValue(DataState.Success(BooksResponse(books = bookList.toList())))
            }
            Constants.CODE_SERVER_ERROR -> {
                _books.postValue(DataState.Fail(message = "Server error"))
            }
            Constants.CODE_AUTHENTICATION_FAIL -> {
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