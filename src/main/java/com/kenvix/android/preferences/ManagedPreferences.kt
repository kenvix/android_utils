//--------------------------------------------------
// Class AbstractPreferencesManager
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.preferences

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.Keep
import androidx.preference.PreferenceManager
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.android.utils.newInstanceFromSerialized
import com.kenvix.android.utils.serializeToString
import com.kenvix.utils.lang.toUnit
import java.io.Serializable
import java.util.*
import kotlin.reflect.KProperty

/**
 * Managed Android Preference
 * @author Kenvix Zure
 * @param preferenceName Android Preferences name
 * @param preferenceAccessMode Android Preferences access mode
 */
@Keep
@Suppress("UNCHECKED_CAST", "unused")
open class ManagedPreferences(
    private val preferenceName: String,
    private val preferenceAccessMode: Int = Context.MODE_PRIVATE
) {
    val preferences = ApplicationEnvironment.appContext.getSharedPreferences(
        preferenceName,
        preferenceAccessMode
    )!!

    @SuppressLint("CommitPrefEdits")
    val preferenceEditor = preferences.edit()!!

    /**
     * This function is running on current thread. if you don't care result
     * Please use [apply()] instead
     */
    fun commit() = preferenceEditor.commit()
    fun apply() = preferenceEditor.apply()
    fun clear() {
        preferenceEditor.clear()
    }

    fun contains(key: String) = preferences.contains(key)
    val all = preferences.all ?: emptyMap<String, Any?>()

    val cachedSerializableObjects: WeakHashMap<String, Serializable>
            by lazy(LazyThreadSafetyMode.NONE) { WeakHashMap<String, Serializable>() }

    inline fun <reified T> get(key: String, defValue: T?): T {
        return when (T::class) {
            Float::class -> preferences.getFloat(key, (defValue ?: 0F) as Float) as T
            Int::class -> preferences.getInt(key, (defValue ?: 0) as Int) as T
            Long::class -> preferences.getLong(key, (defValue ?: 0L) as Long) as T
            Boolean::class -> preferences.getBoolean(key, (defValue ?: false) as Boolean) as T
            String::class -> preferences.getString(key, (defValue ?: "") as String) as T
            Short::class -> preferences.getInt(key, (defValue as Short).toInt()).toShort() as T
            Byte::class -> preferences.getInt(key, (defValue as Byte).toInt()).toByte() as T
            Char::class -> preferences.getInt(key, (defValue as Char).toInt()).toChar() as T
            Serializable::class -> {
                if (cachedSerializableObjects.containsKey(key)) {
                    cachedSerializableObjects[key] as T
                } else {
                    val serialized: String? = preferences.getString(key, null)
                    if (serialized == null) {
                        defValue as T
                    } else {
                        cachedSerializableObjects[key] =
                            T::class.java.newInstanceFromSerialized(serialized) as Serializable
                        cachedSerializableObjects[key] as T
                    }
                }
            }
            Set::class -> preferences.getStringSet(
                key,
                (defValue ?: emptySet<String>()) as Set<String>
            ) as T
            else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName} on $key")
        }
    }

    inline operator fun <reified T> set(key: String, value: T) {
        when (T::class) {
            Float::class -> preferenceEditor.putFloat(key, value as Float)
            Int::class -> preferenceEditor.putInt(key, value as Int)
            Long::class -> preferenceEditor.putLong(key, value as Long)
            Boolean::class -> preferenceEditor.putBoolean(key, value as Boolean)
            String::class -> preferenceEditor.putString(key, value as String)
            Short::class -> preferenceEditor.putInt(key, (value as Short).toInt())
            Byte::class -> preferenceEditor.putInt(key, (value as Byte).toInt())
            Char::class -> preferenceEditor.putInt(key, (value as Char).toInt())
            Serializable::class -> {
                val serialized = (value as Serializable).serializeToString()
                preferenceEditor.putString(key, serialized)
                cachedSerializableObjects[key] = value
            }
            Set::class -> preferenceEditor.putStringSet(key, value as Set<String>)
            else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName} on $key")
        }
    }

    inline operator fun <reified T> get(key: String) = get<T>(key, null)

    inline fun <reified T> preferenceOf(
        key: String? = null,
        defValue: T? = null
    ): DelegatedPreference<T> {
        return object :
            DelegatedPreference<T> {
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
                get(key ?: property.name, defValue)

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
                set(key ?: property.name, value)
        }
    }

    inline fun <reified T> preferenceOf(defValue: T? = null): DelegatedPreference<T> {
        return object :
            DelegatedPreference<T> {
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
                get(property.name, defValue)

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
                set(property.name, value)
        }
    }

    fun applyToPreferenceManager(manager: PreferenceManager) {
        manager.sharedPreferencesName = this.preferenceName
        manager.sharedPreferencesMode = this.preferenceAccessMode
    }

    interface DelegatedPreference<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }
}