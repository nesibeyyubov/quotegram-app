package com.nesib.yourbooknotes.ui.viewmodels

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
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    val mainRepository: MainRepository,
    @ApplicationContext val application: Context
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

    fun something() {
        if (hasInternetConnection()) {
            try {

            } catch (e: Exception) {
                _book.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _book.postValue(DataState.Fail(message = "No internet connection"))
        }
    }

    fun getBook(bookId: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                if (_book.value == null) {
                    _book.postValue(DataState.Loading())
                    val response = mainRepository.getBook(bookId)
                    val handledResponse = handleBookResponse(response)
                    _book.postValue(handledResponse)
                }
            } catch (e: Exception) {
                _book.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _book.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun notifyQuoteRemoved(quote: Quote) = viewModelScope.launch(Dispatchers.Default) {
        quoteList.remove(quote)
        _quotes.postValue(DataState.Success(data = QuotesResponse(quoteList.toList())))
    }

    fun notifyQuoteUpdated(quote: Quote) = viewModelScope.launch(Dispatchers.Default) {
        if (hasInternetConnection()) {
            try {
                val quoteToDelete = quoteList.find { q -> q.id == quote.id }
                val index = quoteList.indexOf(quoteToDelete)
                if (index != -1) {
                    quoteList.remove(quoteToDelete)
                    quoteList.add(index, quote)
                }
                _quotes.postValue(DataState.Success(QuotesResponse(quoteList.toList())))
            } catch (e: Exception) {
                _book.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _book.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun getMoreBookQuotes(bookId: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _quotes.postValue(DataState.Loading())
                val response = mainRepository.getMoreBookQuotes(bookId, page)
                handleQuotesResponse(response)
            } catch (e: Exception) {
                _quotes.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _quotes.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun getBooks(searchText: String? = null, page: Int = 1, notPaginated: Boolean = true) =
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            if (hasInternetConnection()) {
                try {
                    _books.postValue(DataState.Loading())
                    val response = mainRepository.getBooks(searchText, page)
                    handleBooksResponse(response, notPaginated)
                } catch (e: Exception) {
                    _books.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
                }
            } else {
                _books.postValue(DataState.Fail(message = "No internet connection"))
            }
        }

    fun discoverBooks(
        genre: String = "all",
        page: Int = 1,
        notPaginated: Boolean = true,
        searchText: String = ""
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _books.postValue(DataState.Loading())
                val response = mainRepository.discoverBooks(genre, page, searchText)
                handleBooksResponse(response, notPaginated)
            } catch (e: Exception) {
                _books.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _books.postValue(DataState.Fail(message = "No internet connection"))
        }


    }

    fun postBook(name: String, author: String, genre: String, image: File) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        if (hasInternetConnection()) {
            try {
                _book.postValue(DataState.Loading())
                val response = mainRepository.postBook(name, author, genre, image)
                val handledResponse = handleBookResponse(response)
                _book.postValue(handledResponse)
            } catch (e: Exception) {
                _book.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _book.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun toggleBookFollow(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _bookFollow.postValue(DataState.Loading())
                val response = mainRepository.toggleBookFollow(book.id)
                val handledResponse = handleBookResponse(response)
                _book.value?.data?.book?.followers = book.followers
                _book.value?.data?.book?.following = book.following
                _bookFollow.postValue(handledResponse)
            } catch (e: Exception) {
                _bookFollow.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
            }
        } else {
            _bookFollow.postValue(DataState.Fail(message = "No internet connection"))
        }

    }


    private fun handleBookResponse(response: Response<BookResponse>): DataState<BookResponse> {
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

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = application.getSystemService(
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

}