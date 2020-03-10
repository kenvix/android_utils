@file:JvmName("Utils")
package com.kenvix.android.utils

import android.util.Base64
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.android.R
import com.kenvix.android.exception.*
import java.io.*
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.TimeoutException


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

fun Serializable.serializeToBytes(): ByteArray {
    return ByteArrayOutputStream().use { bytes ->
        ObjectOutputStream(bytes).use { obj ->
            obj.writeObject(this)
        }

        bytes.toByteArray()
    }
}

fun Serializable.serializeToString(): String {
    return Base64.encodeToString(serializeToBytes(), Base64.NO_WRAP)
}

fun <T> Class<T>.newInstanceFromSerialized(byteArray: ByteArray): T {
    return ByteArrayInputStream(byteArray).use { bytes ->
        ObjectInputStream(bytes).use { obj ->
            val result = obj.readObject()

            if (!this.isInstance(result))
                throw IllegalArgumentException("Input bytes are not supertype of this class")

            @Suppress("UNCHECKED_CAST")
            result as T
        }
    }
}

fun <T> Class<T>.newInstanceFromSerialized(string: String): T {
    return newInstanceFromSerialized(Base64.decode(string, Base64.NO_WRAP))
}