package com.kenvix.android.utils

import android.content.Context
import android.util.Log
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.android.R
import com.kenvix.android.exception.*
import com.kenvix.android.ui.base.BaseActivityUI
import java.io.PrintStream
import java.lang.Appendable
import java.util.concurrent.TimeoutException

fun Context.exceptionIgnored(execute: () -> Unit) {
    try {
        execute()
    } catch (e: Exception) {
        Log.e("IgnoredException", e.toString())
        BaseActivityUI.getAlertBuilder(this, e.toString(),
            getString(ApplicationEnvironment.getAppResourceIdentifier("error_operation_failed","string")),
            null
        ).show()
    }
}

fun printDebug(str: Any?) {
    Log.d("DebugPrint", str?.toString())
}

fun throwExceptionForHttpCode(code: Int): Nothing {
    throw when (code) {
        400, 405 -> BadRequestException(ApplicationEnvironment.getViewString(R.string.BadRequestException))
        401, 511 -> InvalidAuthorizationException(ApplicationEnvironment.getViewString(R.string.InvalidAuthorizationException))
        403 -> ForbiddenOperationException(ApplicationEnvironment.getViewString(R.string.ForbiddenOperationException))
        404, 410 -> NotFoundException(ApplicationEnvironment.getViewString(R.string.NotFoundException))
        408 -> TimeoutException(ApplicationEnvironment.getViewString(R.string.TimeoutException))
        429 -> TooManyRequestException(ApplicationEnvironment.getViewString(R.string.TooManyRequestException))
        500, 502, 503, 504 -> ServerFaultException(ApplicationEnvironment.getViewString(R.string.ServerFaultException), code)
        else -> UnknownServerException(ApplicationEnvironment.getViewString(R.string.UnknownServerException), code)
    }
}