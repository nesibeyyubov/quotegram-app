package com.nesib.yourbooknotes.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.repositories.UserRepository
import com.nesib.yourbooknotes.models.*
import com.nesib.yourbooknotes.utils.Constants
import com.nesib.yourbooknotes.utils.Constants.CODE_SERVER_ERROR
import com.nesib.yourbooknotes.utils.Constants.CODE_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.yourbooknotes.utils.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val _user = MutableLiveData<DataState<UserResponse>>()
    val user: LiveData<DataState<UserResponse>>
        get() = _user

    private val sharedPreferencesRepository = SharedPreferencesRepository(getApplication())

    fun getUser(userId: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        if(_user.value == null){
            _user.postValue(DataState.Loading())
            val id = userId ?: sharedPreferencesRepository.getUser().userId!!
            val response = UserRepository.getUser(id)
            handleResponse(response)
        }
    }

    fun clearUser() {
        sharedPreferencesRepository.clearUser()
    }

    fun followOrUnfollowUser(userId: String) {

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

    private fun handleResponse(response: Response<UserResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                _user.postValue(DataState.Success(response.body()))
            }
            Constants.CODE_CREATION_SUCCESS -> {
                _user.postValue(DataState.Success(response.body()))
            }
            CODE_VALIDATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _user.postValue(DataState.Fail(message = authFailResponse.message))
            }
            CODE_SERVER_ERROR -> {
                _user.postValue(DataState.Fail(message = "Server error"))
            }
            Constants.CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _user.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

}