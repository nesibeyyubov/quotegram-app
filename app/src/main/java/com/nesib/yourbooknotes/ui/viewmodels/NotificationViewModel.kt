package com.nesib.yourbooknotes.ui.viewmodels

import android.app.Application
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

    private val _notifications = MutableLiveData<DataState<NotificationsResponse>>()
    val notifications
        get() = _notifications

    private val _readNotifications = MutableLiveData<DataState<NotificationsResponse>>()
    val readNotifications
        get() = _readNotifications


    fun getNotifications() = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                _notifications.postValue(DataState.Loading())
                val response = mainRepository.getNotifications()
                val handledResponse = handleNotificationsResponse(response)
                if (handledResponse is DataState.Success) {
                    val allNotifications = handledResponse.data!!.notifications!!.toMutableList()
                    shouldReadNotifications = allNotifications.size != 0
                    notificationDao.getNotifications().forEach { notificationEntry ->
                        allNotifications.add(
                            Notification(
                                notificationEntry.id,
                                notificationEntry.likerUserId,
                                notificationEntry.likedQuoteId,
                                notificationEntry.userId,
                                notificationEntry.username,
                                notificationEntry.userPhoto,
                                true
                            )
                        )
                    }
                    allNotifications.reverse()
                    val notificationEntities =
                        handledResponse.data.notifications!!.map { notification ->
                            NotificationEntity(
                                notification.id!!,
                                notification.likerUserId!!,
                                notification.likedQuoteId!!,
                                notification.userId!!,
                                notification.username!!,
                                notification.userPhoto!!,
                                true
                            )
                        }
                    notificationDao.insertNotifications(notificationEntities)

                    _notifications.postValue(
                        DataState.Success(
                            data = NotificationsResponse(
                                notifications = allNotifications,
                            )
                        )
                    )
                } else {
                    _notifications.postValue(handledResponse)
                }
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
                val handledResponse = handleNotificationsResponse(response)
                _readNotifications.postValue(handledResponse)
            } else {
                _readNotifications.postValue(DataState.Fail(message = "No internet connection"))
            }
        } catch (e: Exception) {
            _readNotifications.postValue(DataState.Fail())
        }
    }

    private fun handleNotificationsResponse(response: Response<NotificationsResponse>): DataState<NotificationsResponse> {
        when (response.code()) {
            CODE_SUCCESS -> {
                return DataState.Success(data = response.body())
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
        }
        return DataState.Fail()
    }


}