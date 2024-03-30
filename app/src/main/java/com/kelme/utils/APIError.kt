package com.kelme.utils

/**
 * Created by Amit Gupta on 16-07-2021.
 */
class APIError {
    private val statusCode = 0
    private val message: String? = null

    fun APIError() {}

    fun status(): Int {
        return statusCode
    }

    fun message(): String? {
        return message
    }
}