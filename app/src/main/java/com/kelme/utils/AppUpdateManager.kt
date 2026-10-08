package com.kelme.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

object AppUpdateManager {

   /* private val _updateRequired = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val updateRequired: SharedFlow<String> =
        _updateRequired.asSharedFlow()

    private val dialogShown = AtomicBoolean(false)

    fun notifyUpdateRequired(message: String) {

        // Prevent multiple API calls from showing multiple dialogs
        if (dialogShown.compareAndSet(false, true)) {
            _updateRequired.tryEmit(message)
        }
    }

    fun reset() {
        dialogShown.set(false)
    }*/

    private val _updateRequired = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val updateRequired: SharedFlow<String> =
        _updateRequired.asSharedFlow()

    fun notifyUpdateRequired(message: String) {
        _updateRequired.tryEmit(message)
    }
}