package com.example.my_uz_android.util

sealed class NetworkResult<out T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<out T>(data: T) : NetworkResult<T>(data)
    class Error<out T>(message: String, data: T? = null) : NetworkResult<T>(data, message)
}