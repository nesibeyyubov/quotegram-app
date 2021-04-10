package com.nesib.yourbooknotes.ui.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val userRepository: UserRepository,
    @ApplicationContext val application: Context
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
        _currentUserId = user.userId
        _isAuthenticated = user.userId != null && user.token != null
        currentUser = user
    }

    fun getFollowingGenres() = sharedPreferencesRepository.getFollowingGenres()

    fun logout() = sharedPreferencesRepository.clearUser()


    fun login(email: String, password: String, username: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection()) {
                try {
                    _auth.postValue(DataState.Loading())
                    val loginBody =
                        mapOf("email" to email, "password" to password, "username" to username)
                    val response = userRepository.login(loginBody)
                    if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                        hasLoginError = true
                    }
                    handleResponse(response)

                } catch (e: Exception) {
                    hasLoginError = true
                    _auth.postValue(DataState.Fail())
                }
            } else {
                hasLoginError = true
                _auth.postValue(DataState.Fail(message = "No internet connection"))
            }

        }

    fun signup(
        email: String,
        password: String,
        username: String,
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {

                _auth.postValue(DataState.Loading())
                val response = userRepository.signup(email, password, username)
                if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                    hasSignupError = true
                }
                handleResponse(response)

            } catch (e: Exception) {
                hasSignupError = true
                _auth.postValue(DataState.Fail())
            }
        } else {
            hasSignupError = true
            _auth.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun signupWithGoogle(
        email: String,
        fullname: String,
        username: String,
        profileImage: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _auth.postValue(DataState.Loading())
                val response =
                    userRepository.signupWithGoogle(email, fullname, username, profileImage)
                if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                    hasSignupError = true
                }
                handleResponse(response)
            } catch (e: Exception) {
                hasSignupError = true
                _auth.postValue(DataState.Fail())
            }
        } else {
            hasSignupError = true
            _auth.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun signInWithGoogle(
        email: String,
        profileImage: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                _auth.postValue(DataState.Loading())
                val response =
                    userRepository.signInWithGoogle(email, profileImage)
                if (response.code() != CODE_SUCCESS && response.code() != CODE_CREATION_SUCCESS) {
                    hasLoginError = true
                }
                handleResponse(response)

            } catch (e: Exception) {
                Log.d("mytag", "signInWithGoogle: catch block: ${e.message}")
                hasLoginError = true
                _auth.postValue(DataState.Fail())
            }
        } else {
            Log.d("mytag", "no internet connnection")
            hasLoginError = true
            _auth.postValue(DataState.Fail(message = "No internet connection"))
        }

    }

    fun getUser() = sharedPreferencesRepository.getCurrentUser()

    fun saveUser() {
        if (auth.value?.data != null) {
            sharedPreferencesRepository.saveUser(auth.value?.data!!)
        }
    }

    fun updateUser() {
        sharedPreferencesRepository.saveUser(currentUser!!)
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
            if (hasInternetConnection()) {
                try {
                    _genres.postValue(DataState.Loading())
                    sharedPreferencesRepository.saveFollowingGenres(genres)
                    if (userId == null) {
                        _genres.postValue(DataState.Success())
                    } else {
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
                } catch (e: Exception) {
                    _genres.postValue(DataState.Fail(message = "Something went wrong: ${e.message}"))
                }
            } else {
                _genres.postValue(DataState.Fail(message = "No internet connection"))
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