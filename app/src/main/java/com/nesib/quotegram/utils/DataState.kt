package com.nesib.quotegram.utils

open class DataState<T>(val data: T? = null, val message: String = "Something went wrong") {
    class Success<T>(data: T? = null, message: String = "Successful") : DataState<T>(data, message)
    class Fail<T>(
        data: T? = null,
        message: String = "Something went wrong,\nplease make sure you have \nactive internet connection"
    ) : DataState<T>(data, message)

    class Loading<T>() : DataState<T>()
}