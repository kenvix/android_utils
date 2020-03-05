//--------------------------------------------------
// Class DefaultUncaughtExceptionHandler
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.utils

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.kenvix.android.ApplicationProperties

object DefaultUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    @JvmStatic private lateinit var context: Context
    @JvmStatic private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    @JvmStatic
    fun setupHandler(context: Context) {
        this.context = context
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        if (e == null) return

        Thread {
            Looper.prepare()
            Log.e("UncaughtException", "From thread ${t?.toString()}, will restart application now!", e)

            Toast.makeText(context, "发生预期外的异常，程序即将重新启动，请报告这个问题。\r\n${e.javaClass.simpleName}: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Looper.loop()
        }.start()

        if (ApplicationProperties.AutoRestartWhenCrash)
            restartApplication()
    }

    private fun restartApplication() {
        Thread.sleep(700)
        val intent = Intent(context, ApplicationProperties.MainActivityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        Thread.sleep(150)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}