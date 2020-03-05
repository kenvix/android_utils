package com.kenvix.android.utils

import android.content.Context
import android.util.Log
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.android.ui.base.BaseActivityUI
import java.io.PrintStream
import java.lang.Appendable

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