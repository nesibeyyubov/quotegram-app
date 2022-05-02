package com.nesib.quotegram.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.quotegram.data.local.SharedPreferencesRepository
import com.nesib.quotegram.data.repositories.UserRepository
import com.nesib.quotegram.models.*
import com.nesib.quotegram.utils.Constants.CODE_AUTHENTICATION_FAIL
import com.nesib.quotegram.utils.Constants.CODE_CREATION_SUCCESS
import com.nesib.quotegram.utils.Constants.CODE_SERVER_ERROR
import com.nesib.quotegram.utils.Constants.CODE_SUCCESS
import com.nesib.quotegram.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.quotegram.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val userRepository: UserRepository, application: Application,
) : AndroidViewModel(application) {
    private var userQuoteList = mutableListOf<Quote>()
    private var usersList = mutableListOf<User>()

    private val _user = MutableLiveData<DataState<UserResponse>>()
    val user: LiveData<DataState<UserResponse>>
        get() = _user

    private val _userFollow = MutableLiveData<DataState<UserResponse>>()
    val userFollow: LiveData<DataState<UserResponse>>
        get() = _userFollow

    private val _userQuotes = MutableLiveData<DataState<QuotesResponse>>()
    val userQuotes: LiveData<DataState<QuotesResponse>>
        get() = _userQuotes

    private val _users = MutableLiveData<DataState<UsersResponse>>()
    val users: LiveData<DataState<UsersResponse>>
        get() = _users


    fun getMoreUserQuotes(userId: String? = null, page: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection()) {
                try {
                    _userQuotes.postValue(DataState.Loading())
                    val id = userId ?: sharedPreferencesRepository.getCurrentUser()?.userId!!
                    val response = userRepository.getMoreUserQuotes(id, page)
                    handleQuotesResponse(response)
                } catch (e: Exception) {
                    _userQuotes.postValue(DataState.Fail())
                }
            } else {
                _userQuotes.postValue(DataState.Fail(message = "No internet connection"))
            }

        }

    fun getUser(userId: String? = null, forced: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection()) {
                try {
                    if (_user.value == null || forced) {
                        _user.postValue(DataState.Loading())
                        val id = userId ?: sharedPreferencesRepository.getCurrentUser()?.userId!!
                        val response = userRepository.getUser(id)
                        val handledResponse = handleUserResponse(response)
                        _user.postValue(handledResponse)
                    }
                } catch (e: Exception) {
                    _user.postValue(DataState.Fail())
                }
            } else {
                _user.postValue(DataState.Fail(message = "No internet connection"))
            }

        }

    fun notifyQuoteRemoved(quote: Quote) {
        userQuoteList.remove(quote)
        _userQuotes.postValue(DataState.Success(data = QuotesResponse(userQuoteList.toList())))
    }

    fun notifyQuoteUpdated(quote: Quote) = viewModelScope.launch(Dispatchers.Default) {
        if (hasInternetConnection()) {
            try {
                val quoteToDelete = userQuoteList.find { q -> q.id == quote.id }
                val index = userQuoteList.indexOf(quoteToDelete)
                if (index != -1) {
                    userQuoteList.remove(quoteToDelete)
                    userQuoteList.add(index, quote)
                }
                _userQuotes.postValue(DataState.Success(QuotesResponse(userQuoteList.toList())))
            } catch (e: Exception) {
                _userQuotes.postValue(DataState.Fail())
            }
        } else {
            _userQuotes.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun getUsers(searchQuery: String = "", paginating: Boolean = false, currentPage: Int = 1) =
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection()) {
                try {
                    _users.postValue(DataState.Loading())
                    val response = userRepository.getUsers(searchQuery, currentPage)
                    handleUsersResponse(response, paginating)
                } catch (e: Exception) {
                    _users.postValue(DataState.Fail())
                }
            } else {
                _users.postValue(DataState.Fail(message = "No internet connection"))
            }

        }


    fun followOrUnFollowUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _userFollow.postValue(DataState.Loading())
                val response = userRepository.followOrUnFollowUser(user.id)
                val handledResponse = handleUserResponse(response)
                _userFollow.postValue(handledResponse)
            } catch (e: Exception) {
                _userFollow.postValue(DataState.Fail())
            }
        } else {
            _userFollow.postValue(DataState.Fail(message = "No internet connection"))
        }

    }


    fun updateUser(
        username: String,
        fullname: String,
        bio: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (hasInternetConnection()) {
                _user.postValue(DataState.Loading())
                val body = mapOf("username" to username, "fullname" to fullname, "bio" to bio)
                val response = userRepository.updateUser(body)
                val handledResponse = handleUserResponse(response, true)
                _user.postValue(handledResponse)
            } else {
                _user.postValue(DataState.Fail(message = "No internet connection"))
            }
        } catch (e: Exception) {
            _user.postValue(DataState.Fail(message = "Something went wrong: ${e.cause}"))
        }
    }

    private fun handleQuotesResponse(response: Response<QuotesResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                response.body()!!.quotes.forEach { quote ->
                    removeIfHtmlTagsExist(quote)
                    userQuoteList.add(quote)
                }
                _userQuotes.postValue(DataState.Success(QuotesResponse(userQuoteList.toList())))
            }
            CODE_CREATION_SUCCESS -> {

            }
            CODE_VALIDATION_FAIL -> {

            }
            CODE_SERVER_ERROR -> {
                _userQuotes.postValue(DataState.Fail(message = "Server error"))
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _userQuotes.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }


    private fun handleUsersResponse(response: Response<UsersResponse>, paginating: Boolean) {
        when (response.code()) {
            CODE_SUCCESS -> {
                if (paginating) {
                    response.body()?.users?.forEach { user ->
                        usersList.add(user)
                    }
                } else {
                    usersList = response.body()?.users?.toMutableList() ?: mutableListOf()
                }
                _users.postValue(
                    DataState.Success(
                        UsersResponse(
                            users = usersList,
                            message = "Got the users successfully"
                        )
                    )
                )
            }
            CODE_SERVER_ERROR -> {
                _users.postValue(DataState.Fail(message = "Server error"))
            }
        }
    }

    private fun removeIfHtmlTagsExist(quote: Quote) {
        if (quote.quote?.contains("<br>") == true) {
            quote.quote = quote.quote?.replaceFirst("<br>", "\n")
            quote.quote = quote.quote?.replaceFirst("<br>", "\n")
            quote.quote = quote.quote?.replace("<br>", "")
        }
    }

    private fun handleUserResponse(
        response: Response<UserResponse>,
        userUpdated: Boolean = false
    ): DataState<UserResponse> {
        when (response.code()) {
            CODE_SUCCESS -> {
                if (!userUpdated) {
                    userQuoteList = response.body()!!.user!!.quotes!!.toMutableList()
                }
                response.body()?.user?.quotes?.forEach { quote -> removeIfHtmlTagsExist(quote) }
                return DataState.Success(response.body())
            }
            CODE_CREATION_SUCCESS -> {
                return DataState.Success(response.body())
            }
            CODE_VALIDATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                return DataState.Fail(message = authFailResponse.message)
            }
            CODE_SERVER_ERROR -> {
                return DataState.Fail(message = "Server error")
            }
            CODE_AUTHENTICATION_FAIL -> {
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

}