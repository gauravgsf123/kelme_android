package com.kelme.utils

/**
 * Created by Amit on 28,June,2021
 */
sealed class Resource<T>(
    val message: String? = null,
    val data: T? = null

) {
    class Success<T>(message: String, data: T) : Resource<T>(message, data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(message, data)
    class Loading<T> : Resource<T>()
}