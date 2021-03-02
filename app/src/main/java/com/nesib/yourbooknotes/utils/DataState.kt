package com.nesib.yourbooknotes.utils

open class DataState<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T? = null, message: String? = null) : DataState<T>(data, message)
    class Fail<T>(data: T? = null, message: String? = null) : DataState<T>(data, message)
    class Loading<T>() : DataState<T>()
}