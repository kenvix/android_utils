//--------------------------------------------------
// Class PreferenceTest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android

import android.util.Log
import com.kenvix.android.preferences.ManagedPreferences
import org.junit.Test

class PreferenceTest {
    @Test
    fun test() {
        val test = ManagedPreferences("fuck")
        val str: String = test["fff"]
        test["fff"] = "asdasdasddasdas"
    }
}