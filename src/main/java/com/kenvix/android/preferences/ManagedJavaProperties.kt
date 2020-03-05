//--------------------------------------------------
// Class ManagedJavaProperties
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.preferences

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.reflect.KProperty

/**
 * Managed java properties
 * @author Kenvix Zure
 * @param inputStream Input stream of java properties
 * @param outputStream Output stream of java properties, if null, this properties will be read only
 */
open class ManagedJavaProperties(val inputStream: InputStream, val outputStream: OutputStream? = null) {
    /**
     * Initialize Managed java properties from a file
     */
    constructor(file: File): this(file.inputStream(), file.outputStream())

    /**
     * Initialize Managed java properties from a file name
     */
    constructor(fileName: String): this(File(fileName))

    /**
     * Initialize readonly Managed java properties from a java resources file name
     */
    constructor(clazz: Class<*>, fileName: String):
            this(clazz.getResourceAsStream(fileName) ?: throw FileNotFoundException("Properties not found"))

    val properties = Properties()

    init {
        properties.load(inputStream)
    }

    operator fun get(key: String) = properties.getProperty(key)

    inline fun <reified T> get(key: String, defValue: T): T {
        return when (T::class) {
            String::class -> properties.getProperty(key, defValue as String) as T
            Int::class -> properties.getProperty(key, defValue.toString()).toInt() as T
            Long::class -> properties.getProperty(key, defValue.toString()).toLong()  as T
            Boolean::class -> properties.getProperty(key, defValue.toString())!!.toBoolean() as T
            Double::class -> properties.getProperty(key, defValue.toString()).toDouble() as T
            Short::class -> properties.getProperty(key, defValue.toString()).toShort() as T
            Float::class -> properties.getProperty(key, defValue.toString()).toFloat() as T
            else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName}")
        }
    }

    inline operator fun <reified T> set(key: String, value: T) {
        when (T::class) {
            String::class -> properties.setProperty(key, value as String)
            else -> properties.setProperty(key, value.toString())
        }
    }

    fun save(description: String? = null) {
        if (outputStream == null)
            throw IllegalArgumentException("Due to outputStream is null, this properties is readonly and cannot be saved")

        properties.store(outputStream, description)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> propertyOf(key: String, defValue: T): DelegatedProperties<T> {
        return object : DelegatedProperties<T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T
                    = get(key, defValue)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
                    = set(key, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> propertyOf(defValue: T): DelegatedProperties<T> {
        return object : DelegatedProperties<T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T
                    = get(property.name, defValue)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
                    = set(property.name, value)
        }
    }

    interface DelegatedProperties<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }
}