package com.nesib.quotegram.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import com.nesib.quotegram.data.repositories.MainRepository
import com.nesib.quotegram.models.BasicResponse
import com.nesib.quotegram.utils.Constants.CODE_CREATION_SUCCESS
import com.nesib.quotegram.utils.Constants.CODE_SERVER_ERROR
import com.nesib.quotegram.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    val mainRepository: MainRepository, application: Application,
) : AndroidViewModel(application) {
    private val _report = MutableLiveData<DataState<BasicResponse>>()
    val report: LiveData<DataState<BasicResponse>>
        get() = _report


    fun reportQuote(reporterId: String, reportedQuoteId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (hasInternetConnection()) {
                    _report.postValue(DataState.Loading())
                    val reportBody =
                        mapOf("reportedQuoteId" to reportedQuoteId, "reporterId" to reporterId )
                    val response = mainRepository.reportQuote(reportBody)
                    handleReportResponse(response)
                } else {
                    _report.postValue(DataState.Fail(message = "No internet connection"))
                }
            } catch (e: Exception) {
                _report.postValue(DataState.Fail())
            }
        }

    fun reportUser(reporterId: String, reportedUserId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (hasInternetConnection()) {
                    _report.postValue(DataState.Loading())
                    val reportBody =
                        mapOf("reporterId" to reporterId, "reportedUserId" to reportedUserId)
                    val response = mainRepository.reportUser(reportBody)
                    handleReportResponse(response)
                } else {
                    _report.postValue(DataState.Fail(message = "No internet connection"))
                }
            } catch (e: Exception) {
                _report.postValue(DataState.Fail())
            }
        }

    fun reportBook(reporterId: String, reportedBookId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (hasInternetConnection()) {
                    _report.postValue(DataState.Loading())
                    val reportBody =
                        mapOf("reporterId" to reporterId, "reportedBookId" to reportedBookId)
                    val response = mainRepository.reportBook(reportBody)
                    handleReportResponse(response)
                } else {
                    _report.postValue(DataState.Fail(message = "No internet connection"))
                }
            } catch (e: Exception) {
                _report.postValue(DataState.Fail())
            }
        }

    private fun handleReportResponse(response: Response<BasicResponse>) {
        when (response.code()) {
            CODE_CREATION_SUCCESS -> {
                _report.postValue(DataState.Success(response.body()))
            }
            CODE_SERVER_ERROR -> {
                _report.postValue(DataState.Fail(message = "Something went wrong in server,please try again later"))
            }
        }
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