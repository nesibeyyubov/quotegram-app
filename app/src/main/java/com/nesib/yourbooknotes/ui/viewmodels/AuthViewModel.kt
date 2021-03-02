package com.nesib.yourbooknotes.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.repositories.UserRepository
import com.nesib.yourbooknotes.models.AuthFailResponse
import com.nesib.yourbooknotes.models.AuthResponse
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.utils.Constants
import com.nesib.yourbooknotes.utils.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel : ViewModel() {
    private val _auth = MutableLiveData<DataState<AuthResponse>>()
    val auth: LiveData<DataState<AuthResponse>>
        get() = _auth


    fun login(email: String, password: String){
        _auth.value = DataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val response = UserRepository.login(email,password)
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
        viewModelScope.launch(Dispatchers.IO){
            val response = UserRepository.signup(email,password,fullname,username)
            handleResponse(response)
        }
    }

    private fun handleResponse(response:Response<AuthResponse>){

        when(response.code()){
            Constants.CODE_SUCCESS ->{
                _auth.postValue(DataState.Success(response.body()))
            }
            Constants.CODE_VALIDATION_FAIL ->{
                val authFailResponse = Gson().fromJson(response.errorBody()?.charStream(),AuthFailResponse::class.java)
                _auth.postValue(DataState.Fail(message = authFailResponse.message))
            }
            Constants.CODE_SERVER_ERROR ->{
                _auth.postValue(DataState.Fail(message = "Server error"))
            }
            Constants.CODE_AUTHENTICATION_FAIL ->{
                val authFailResponse = Gson().fromJson(response.errorBody()?.charStream(),AuthFailResponse::class.java)
                _auth.postValue(DataState.Fail(message = authFailResponse.message))
            }
        }
    }

    fun saveFollowingGenres(genres: Map<String, String>) {

    }
}