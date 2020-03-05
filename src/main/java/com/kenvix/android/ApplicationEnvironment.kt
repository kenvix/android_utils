@file:JvmName("ApplicationEnvironment")
@file:Suppress("ConstantConditionIf")

package com.kenvix.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.util.Log
import androidx.annotation.NonNull
import com.kenvix.utils.log.Logging
import com.kenvix.android.utils.AndroidLoggingHandler
import com.kenvix.android.utils.DefaultUncaughtExceptionHandler
import com.kenvix.utils.log.LoggingOutputStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.PrintStream
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class ApplicationEnvironment : Application(), Logging {
    override fun getLogTag(): String = "ApplicationEnvironment"

    companion object Utils {
        @JvmStatic val KenvixAndroidUtilsVersionCode = 13

        @NonNull
        @JvmStatic
        lateinit var appContext: Context

        @SuppressLint("StaticFieldLeak")
        @NonNull
        @JvmStatic
        lateinit var rootContext: Context

        @SuppressLint("StaticFieldLeak")
        @NonNull
        @JvmStatic
        lateinit var instance: ApplicationEnvironment


        val okHttpClient: OkHttpClient by lazy {
            val okHttpClientBuilder = OkHttpClient.Builder().
                    connectTimeout(ApplicationProperties.OkHttpClientTimeout, TimeUnit.SECONDS).
                    followRedirects(true)

            val cacheDir = appContext.cacheDir.resolve("okhttp")

            if (cacheDir.exists())
                cacheDir.mkdirs()

            okHttpClientBuilder.cache(Cache(cacheDir, ApplicationProperties.OkHttpClientCacheSize))
            okHttpClientBuilder.build()
        }

        @JvmStatic
        fun getAppResourceIdentifier(name: String, type: String): Int {
            return appContext.resources.getIdentifier(name, type, ApplicationProperties.MainAppPackageName)
        }

        @JvmStatic
        fun getViewString(id: Int, vararg formatArgs: Any): String {
            return appContext.getString(id, *formatArgs)
        }

        @JvmStatic
        fun getViewString(id: Int): String {
            return appContext.getString(id)
        }

        @JvmStatic
        fun getViewColor(id: Int): Int {
            return appContext.getColor(id)
        }

        @JvmStatic
        fun getViewDrawable(id: Int): Drawable? {
            return appContext.getDrawable(id)
        }

        @JvmStatic
        fun getRawResourceUri(id: Int) = "android.resource://${appContext.packageName}/$id"

        @JvmStatic
        val viewResources
            get() = appContext.resources

        val defaultSharedPreferences: SharedPreferences
            get() = PreferenceManager.getDefaultSharedPreferences(appContext)

        @JvmStatic
        lateinit var cachedThreadPool: ThreadPoolExecutor
            private set

        @JvmStatic
        lateinit var timer: Timer
            internal set

        var userApplicationEnvironment: ApplicationEnvironment.UserApplication? = null
            internal set

        @JvmStatic
        fun getPackageName(name: String) = ApplicationProperties.MainAppPackageName + "." + name

        @Suppress("unused")
        @JvmStatic
        fun getAppContextEx(): Context = appContext
    }

    override fun onCreate() {
        super.onCreate()

        NativeCalls.initialize(this)

        AndroidLoggingHandler.applyToKenvixLogger()

        if (ApplicationProperties.UserApplicationClassPath.isNotBlank()) {
            val userApplicationEnvironmentClass = Class.forName(ApplicationProperties.UserApplicationClassPath) as Class<*>
            userApplicationEnvironment = userApplicationEnvironmentClass.newInstance() as UserApplication
        }

        cachedThreadPool = ThreadPoolExecutor(1, 20,
                60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>()
        )
        cachedThreadPool.setRejectedExecutionHandler { r: Runnable?, executor: ThreadPoolExecutor? ->
            logger.severe("GlobalThreadPool: ${executor.toString()}")
            userApplicationEnvironment?.onThreadPoolReject(r, executor)
        }

        GlobalScope.launch {
            DefaultUncaughtExceptionHandler.setupHandler(appContext)

            if (ApplicationProperties.RedirectStandardOutput) {
                System.setErr(PrintStream(LoggingOutputStream(Logger.getLogger("StandardError"), Level.SEVERE)))
                System.setOut(PrintStream(LoggingOutputStream(Logger.getLogger("StandardOutput"), Level.INFO)))
            }

            NativeCalls.initializeAsync()
        }

        timer = Timer()

        userApplicationEnvironment?.onCreate()
        Log.d(logTag, "Initialized version: $KenvixAndroidUtilsVersionCode")
    }

    interface UserApplication {
        fun onCreate()
        fun onThreadPoolReject(r: Runnable?, executor: ThreadPoolExecutor?)
    }
}