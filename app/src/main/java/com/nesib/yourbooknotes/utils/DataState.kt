package com.nesib.yourbooknotes.utils

open class DataState<T>(val data: T? = null, val message: String = "Something went wrong") {
    class Success<T>(data: T? = null, message: String = "Successful") : DataState<T>(data, message)
    class Fail<T>(data: T? = null, message: String = "Something went wrong") : DataState<T>(data, message)
    class Loading<T>() : DataState<T>()
}