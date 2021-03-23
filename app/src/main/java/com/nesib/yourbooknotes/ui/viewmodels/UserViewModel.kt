package com.nesib.yourbooknotes.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.repositories.UserRepository
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants
import com.nesib.yourbooknotes.utils.Constants.CODE_AUTHENTICATION_FAIL
import com.nesib.yourbooknotes.utils.Constants.CODE_CREATION_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_SERVER_ERROR
import com.nesib.yourbooknotes.utils.Constants.CODE_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.yourbooknotes.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val userRepository: UserRepository
) : ViewModel() {
    private var userQuoteList = mutableListOf<Quote>()

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
            _userQuotes.postValue(DataState.Loading())
            val id = userId ?: sharedPreferencesRepository.getUser().userId!!
            val response = userRepository.getMoreUserQuotes(id, page)
            handleQuotesResponse(response)
        }

    fun getUser(userId: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        if (_user.value == null) {
            _user.postValue(DataState.Loading())
            val id = userId ?: sharedPreferencesRepository.getUser().userId!!
            val response = userRepository.getUser(id)
            val handledResponse = handleUserResponse(response)
            _user.postValue(handledResponse)
        }
    }

    fun notifyQuoteRemoved(quote:Quote){
        userQuoteList.remove(quote)
        _userQuotes.postValue(DataState.Success(data = QuotesResponse(userQuoteList.toList())))
    }
    fun notifyQuoteUpdated(quote:Quote) = viewModelScope.launch(Dispatchers.Default) {
        val quoteToDelete = userQuoteList.find{q -> q.id == quote.id}
        val index = userQuoteList.indexOf(quoteToDelete)
        if(index != -1){
            userQuoteList.remove(quoteToDelete)
            userQuoteList.add(index,quote)
        }
        _userQuotes.postValue(DataState.Success(QuotesResponse(userQuoteList.toList())))
    }

    fun getUsers(searchQuery: String = "") = viewModelScope.launch(Dispatchers.IO) {
        _users.postValue(DataState.Loading())
        val response = userRepository.getUsers(searchQuery)
        handleUsersResponse(response)
    }


    fun followOrUnFollowUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        _userFollow.postValue(DataState.Loading())
        val response = userRepository.followOrUnFollowUser(user.id)
        val handledResponse = handleUserResponse(response)
        _userFollow.postValue(handledResponse)
    }

    fun saveFollowingGenres(genres: Map<String, String>) {

    }

    fun updateUser(
        username: String?,
        fullname: String?,
        email: String?,
        password: String?,
        bio: String?
    ) {

    }


    fun getSavedQuotes(userId: String) {

    }

    fun postSavedQuote(quoteId: String) {

    }

    private fun handleQuotesResponse(response: Response<QuotesResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                response.body()!!.quotes.forEach { quote -> userQuoteList.add(quote) }
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

    private fun handleUsersResponse(response: Response<UsersResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                _users.postValue(DataState.Success(response.body()))
            }
            CODE_SERVER_ERROR -> {
                _users.postValue(DataState.Fail(message = "Server error"))
            }
        }
    }

    private fun handleUserResponse(response: Response<UserResponse>):DataState<UserResponse> {
        when (response.code()) {
            CODE_SUCCESS -> {
                userQuoteList = response.body()!!.user!!.quotes!!.toMutableList()
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

}