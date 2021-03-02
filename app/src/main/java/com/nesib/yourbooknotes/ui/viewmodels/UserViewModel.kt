package com.nesib.yourbooknotes.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.repositories.UserRepository
import com.nesib.yourbooknotes.models.AuthResponse
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.utils.Constants.CODE_SERVER_ERROR
import com.nesib.yourbooknotes.utils.Constants.CODE_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.yourbooknotes.utils.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<DataState<User>>()
    val user: LiveData<DataState<User>>
        get() = _user

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

    fun getUser(userId: String) {

    }

    fun getSavedQuotes(userId: String) {

    }

    fun postSavedQuote(quoteId: String){

    }

}