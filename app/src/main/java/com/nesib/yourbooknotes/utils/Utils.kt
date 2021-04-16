package com.nesib.yourbooknotes.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import com.nesib.yourbooknotes.models.BasicResponse
import retrofit2.Response

object Utils {
    var usersNotifier:IUsersNotifer? = null
    var booksNotifier:IBooksNotifer? = null
    fun <T> toBasicResponse(response:Response<T>):BasicResponse{
        return Gson().fromJson(
            response.errorBody()?.charStream(),
            BasicResponse::class.java
        )
    }

    fun hasInternetConnection(application:Application): Boolean {
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