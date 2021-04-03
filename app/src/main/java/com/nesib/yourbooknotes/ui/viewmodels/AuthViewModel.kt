package com.nesib.yourbooknotes.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.local.SharedPreferencesRepository
import com.nesib.yourbooknotes.data.repositories.UserRepository
import com.nesib.yourbooknotes.models.*
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
    var currentUser: UserAuth? = null

    private var _isAuthenticated = false
    val isAuthenticated
        get() = _isAuthenticated

    private var _currentUserId: String? = null
    val currentUserId
        get() = _currentUserId

    private val _auth = MutableLiveData<DataState<UserAuth>>()
    val auth: LiveData<DataState<UserAuth>>
        get() = _auth

    private val _genres = MutableLiveData<DataState<UserAuth>>()
    val genres: LiveData<DataState<UserAuth>>
        get() = _genres

    init {
        initAuthentication()
    }


    private fun initAuthentication() {
        val user = sharedPreferencesRepository.getCurrentUser()
        _currentUserId = user?.userId
        _isAuthenticated = user?.userId != null
        currentUser = user
    }

    fun getFollowingGenres() = sharedPreferencesRepository.getFollowingGenres()

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
        username: String,
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

    fun signupWithGoogle(
        email: String,
        fullname: String,
        username: String,
        profileImage: String
    ) {
        _auth.value = DataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val response =
                userRepository.signupWithGoogle(email, fullname, username, profileImage)
            if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                hasSignupError = true
            }
            handleResponse(response)
        }
    }

    fun signInWithGoogle(
        email: String,
        profileImage: String
    ) {
        _auth.value = DataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val response =
                userRepository.signInWithGoogle(email, profileImage)
            if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                hasLoginError = true
            }
            handleResponse(response)
        }
    }

    fun getUser() = sharedPreferencesRepository.getCurrentUser()

    fun saveUser() {
        if(auth.value?.data != null){
            sharedPreferencesRepository.saveUser(auth.value?.data!!)
        }
    }

    private fun handleResponse(response: Response<UserAuth>) {
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

    fun saveFollowingGenres(genres: String, userId: String? = null) =
        viewModelScope.launch(Dispatchers.IO) {
            _genres.postValue(DataState.Loading())
            sharedPreferencesRepository.saveFollowingGenres(genres)
            if (userId == null) {
                _genres.postValue(DataState.Success())
            } else {
                Log.d("mytag", "saving genres in backend...")
                val response = userRepository.saveFollowingGenres(genres)
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