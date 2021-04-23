package com.nesib.yourbooknotes.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nesib.yourbooknotes.data.local.AppDatabase
import com.nesib.yourbooknotes.data.local.NotificationDao
import com.nesib.yourbooknotes.data.repositories.MainRepository
import com.nesib.yourbooknotes.models.Notification
import com.nesib.yourbooknotes.models.NotificationEntity
import com.nesib.yourbooknotes.models.NotificationsResponse
import com.nesib.yourbooknotes.utils.Constants.CODE_AUTHENTICATION_FAIL
import com.nesib.yourbooknotes.utils.Constants.CODE_SERVER_ERROR
import com.nesib.yourbooknotes.utils.Constants.CODE_SUCCESS
import com.nesib.yourbooknotes.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    application: Application,
    val mainRepository: MainRepository,
    val notificationDao: NotificationDao
) : AndroidViewModel(application) {
    var shouldReadNotifications = false
    private val notificationList = mutableListOf<Notification>()

    private val _notifications = MutableLiveData<DataState<NotificationsResponse>>()
    val notifications
        get() = _notifications

    private val _readNotifications = MutableLiveData<DataState<NotificationsResponse>>()
    val readNotifications
        get() = _readNotifications


    fun getNotifications(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                _notifications.postValue(DataState.Loading())
                val response = mainRepository.getNotifications(page)
                handleNotificationsResponse(response)
            } else {
                _notifications.postValue(DataState.Fail(message = "No internet connection"))
            }
        } catch (e: Exception) {
            _notifications.postValue(DataState.Fail())
        }
    }


    fun readNotifications() = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                _readNotifications.postValue(DataState.Loading())
                val response = mainRepository.readNotifications()
                handleNotificationsResponse(response)
            } else {
                _readNotifications.postValue(DataState.Fail(message = "No internet connection"))
            }
        } catch (e: Exception) {
            _readNotifications.postValue(DataState.Fail())
        }
    }

    private fun handleNotificationsResponse(response: Response<NotificationsResponse>) {
        when (response.code()) {
            CODE_SUCCESS -> {
                response.body()?.notifications?.forEach { notification ->
                    notificationList.add(notification)
                }
                _notifications.postValue(
                    DataState.Success(
                        data = NotificationsResponse(
                            notifications = notificationList
                        )
                    )
                )
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authResponse = Utils.toBasicResponse(response)
                _notifications.postValue(DataState.Fail(message = authResponse.message))
            }
            CODE_SERVER_ERROR -> {
                val serverResponse = Utils.toBasicResponse(response)
                _notifications.postValue(DataState.Fail(message = serverResponse.message))
            }
            CODE_VALIDATION_FAIL -> {
                val validationResponse = Utils.toBasicResponse(response)
                _notifications.postValue(DataState.Fail(message = validationResponse.message))
            }
            else -> {
                _notifications.postValue(DataState.Fail())
            }
        }
    }


}