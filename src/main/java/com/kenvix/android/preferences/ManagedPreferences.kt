//--------------------------------------------------
// Class AbstractPreferencesManager
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.preferences

import android.content.Context
import androidx.annotation.Keep
import androidx.preference.PreferenceManager
import com.kenvix.android.ApplicationEnvironment
import kotlin.reflect.KProperty

/**
 * Managed Android Preference
 * @author Kenvix Zure
 * @param name Android Preferences name
 * @param mode Android Preferences access mode
 */
@Keep
@Suppress("UNCHECKED_CAST", "unused")
open class ManagedPreferences(val name: String, val mode: Int = Context.MODE_PRIVATE) {
    val preferences = ApplicationEnvironment.appContext.getSharedPreferences(name, mode)
    val editor = preferences.edit()

    fun commit() = editor.commit()
    fun clear() = editor.clear()

    inline fun <reified T> get(key: String, defValue: T?): T {
        return when (T::class) {
            Float::class -> preferences.getFloat(key, (defValue ?: 0F) as Float) as T
            Int::class -> preferences.getInt(key, (defValue ?: 0) as Int) as T
            Long::class -> preferences.getLong(key, (defValue ?: 0L) as Long) as T
            Boolean::class -> preferences.getBoolean(key, (defValue ?: false) as Boolean) as T
            String::class -> preferences.getString(key, (defValue ?: "") as String) as T
            Set::class -> preferences.getStringSet(key, (defValue ?: emptySet<String>()) as Set<String>) as T
            else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName} on $key")
        }
    }

    inline operator fun <reified T> set(key: String, value: T) {
        when (T::class) {
            Float::class -> editor.putFloat(key, value as Float)
            Int::class -> editor.putInt(key, value as Int)
            Long::class -> editor.putLong(key, value as Long)
            Boolean::class -> editor.putBoolean(key, value as Boolean)
            String::class -> editor.putString(key, value as String)
            Set::class -> editor.putStringSet(key, value as Set<String>)
            else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName} on $key")
        }
    }

    inline operator fun <reified T> get(key: String) = get<T>(key, null)

    inline fun <reified T> preferenceOf(key: String? = null, defValue: T? = null): DelegatedPreference<T> {
        return object :
            DelegatedPreference<T> {
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T
                = get(key ?: property.name, defValue)

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
                = set(key ?: property.name, value)
        }
    }

    fun applyToPreferenceManager(manager: PreferenceManager) {
        manager.sharedPreferencesName = this.name
        manager.sharedPreferencesMode = this.mode
    }

    interface DelegatedPreference<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }
}