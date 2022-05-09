package com.nesib.quotegram.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nesib.quotegram.data.repositories.MainRepository
import com.nesib.quotegram.models.Notification
import com.nesib.quotegram.models.NotificationsResponse
import com.nesib.quotegram.utils.Constants.CODE_AUTHENTICATION_FAIL
import com.nesib.quotegram.utils.Constants.CODE_SERVER_ERROR
import com.nesib.quotegram.utils.Constants.CODE_SUCCESS
import com.nesib.quotegram.utils.Constants.CODE_VALIDATION_FAIL
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    application: Application,
    val mainRepository: MainRepository,
) : AndroidViewModel(application) {
    var shouldReadNotifications = false
    private val notificationList = mutableListOf<Notification>()

    private val _notifications = MutableLiveData<DataState<NotificationsResponse>>()
    val notifications
        get() = _notifications

    private val _clearNotifications = MutableLiveData<DataState<NotificationsResponse>>()
    val clearNotifications
        get() = _clearNotifications


    fun getNotifications(page: Int = 1, forced: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Utils.hasInternetConnection(getApplication<Application>())) {
                    if (_notifications.value == null || forced) {
                        _notifications.postValue(DataState.Loading())
                        val response = mainRepository.getNotifications(page)
                        val handledResponse = handleNotificationsResponse(response, page)
                        _notifications.postValue(handledResponse)
                    }
                } else {
                    _notifications.postValue(DataState.Fail(message = "No internet connection"))
                }
            } catch (e: Exception) {
                _notifications.postValue(DataState.Fail())
            }
        }


    fun clearNotifications() = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                _clearNotifications.postValue(DataState.Loading())
                val response = mainRepository.clearNotifications()
                val handledResponse = handleNotificationsResponse(response, page = 1)
                _clearNotifications.postValue(handledResponse)
            } else {
                _clearNotifications.postValue(DataState.Fail(message = "No internet connection"))
            }
        } catch (e: Exception) {
            _clearNotifications.postValue(DataState.Fail())
        }
    }

    private fun handleNotificationsResponse(
        response: Response<NotificationsResponse>,
        page: Int
    ): DataState<NotificationsResponse> {
        when (response.code()) {
            CODE_SUCCESS -> {
                if (page == 1) {
                    notificationList.clear()
                }
                response.body()?.notifications?.forEach { notification ->
                    notificationList.add(notification)
                }

                return DataState.Success(
                    data = NotificationsResponse(
                        notifications = notificationList
                    )
                )
            }
            CODE_AUTHENTICATION_FAIL -> {
                val authResponse = Utils.toBasicResponse(response)
                return DataState.Fail(message = authResponse.message)
            }
            CODE_SERVER_ERROR -> {
                val serverResponse = Utils.toBasicResponse(response)
                return DataState.Fail(message = serverResponse.message)
            }
            CODE_VALIDATION_FAIL -> {
                val validationResponse = Utils.toBasicResponse(response)
                return DataState.Fail(message = validationResponse.message)
            }
            else -> {
                return DataState.Fail()
            }
        }
    }


}