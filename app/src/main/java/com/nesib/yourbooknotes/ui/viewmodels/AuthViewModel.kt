package com.nesib.yourbooknotes.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.network.AuthApi
import com.nesib.yourbooknotes.data.repositories.UserRepository
import com.nesib.yourbooknotes.models.BasicResponse
import com.nesib.yourbooknotes.models.AuthResponse
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.models.UserResponse
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
class AuthViewModel @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val userRepository: UserRepository
) : ViewModel() {
    var hasSignupError = false
    var hasLoginError = false

    private var _isAuthenticated = false
    val isAuthenticated
        get() = _isAuthenticated

    private val _auth = MutableLiveData<DataState<AuthResponse>>()
    val auth: LiveData<DataState<AuthResponse>>
        get() = _auth

    private val _genres = MutableLiveData<DataState<UserResponse>>()
    val genres: LiveData<DataState<UserResponse>>
        get() = _genres

    init {
        checkAuthentication()
    }


    private fun checkAuthentication() {
        val user = sharedPreferencesRepository.getUser()
        _isAuthenticated = user.userId != null && user.token != null
    }


    fun getUserId() = sharedPreferencesRepository.getUser().userId


    fun logout() {
        sharedPreferencesRepository.clearUser()
    }

    fun login(email: String, password: String) {
        _auth.value = DataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.login(email, password)
            if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                hasLoginError = true
            }
            handleResponse(response)
        }
    }

    fun signup(
        email: String,
        password: String,
        fullname: String,
        username: String
    ) {
        _auth.value = DataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.signup(email, password, fullname, username)
            if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                hasSignupError = true
            }
            handleResponse(response)
        }
    }

    fun saveExtraUserDetail(username: String, email: String, profileImage: String) =
        sharedPreferencesRepository.saveExtraUserDetail(username, email, profileImage)

    fun getExtraUserDetail() = sharedPreferencesRepository.getExtraUserDetail()

    fun saveUser() {
        val userId = auth.value?.data?.userId
        val token = auth.value?.data?.token
        if (userId != null && token != null) {
            sharedPreferencesRepository.saveUser(userId, token)
        }
    }


    private fun handleResponse(response: Response<AuthResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                _auth.postValue(DataState.Success(response.body()))
            }
            CODE_CREATION_SUCCESS -> {
                _auth.postValue(DataState.Success(response.body()))
            }
            CODE_VALIDATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _auth.postValue(DataState.Fail(message = authFailResponse.message))
            }
            CODE_SERVER_ERROR -> {
                _auth.postValue(DataState.Fail(message = "Server error"))
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authFailResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    BasicResponse::class.java
                )
                _auth.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

    fun saveFollowingGenres(genres: String) {
        _genres.postValue(DataState.Loading())
        if (auth.value?.data?.userId == null) {
            sharedPreferencesRepository.saveFollowingGenres(genres)
            _genres.postValue(DataState.Success())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val response =
                userRepository.saveFollowingGenres(genres, "Bearer " + auth.value!!.data!!.token)
            when (response.code()) {
                CODE_SERVER_ERROR -> {
                    _genres.postValue(DataState.Fail(message = "Something went wrong in server"))
                }
                CODE_AUTHENTICATION_FAIL -> {
                    _genres.postValue(DataState.Fail(message = "You are not authenticated"))
                }
                CODE_SUCCESS -> {
                    _genres.postValue(DataState.Success(response.body()))
                }

            }
        }
    }
}