//--------------------------------------------------
// Class ApplicationProperties
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android

import com.kenvix.android.preferences.ManagedJavaProperties

object ApplicationProperties : ManagedJavaProperties(ApplicationEnvironment::class.java, "/assets/KenvixAndroidUtils.properties") {
    val RedirectStandardOutput: Boolean by propertyOf(false)
    val OkHttpClientTimeout: Long by propertyOf(10L)
    val OkHttpClientCacheSize: Long by propertyOf(1000000000L)

    val MainAppPackageName: String by propertyOf("")
    val MainActivityClassPath: String by propertyOf("")
    val MainActivityClass: Class<*> by lazy { Class.forName(MainActivityClassPath) }
    val UserApplicationClassPath: String by propertyOf("")

    val AutoRestartWhenCrash: Boolean by propertyOf(false)
}