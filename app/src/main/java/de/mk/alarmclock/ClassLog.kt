@file:Suppress("unused")

package de.mk.alarmclock

import android.util.Log.*

fun <T> Class<T>.logInfo(msg: String) = log(INFO, msg)
fun <T> Class<T>.logWarn(msg: String) = log(WARN, msg)
fun <T> Class<T>.logError(msg: String) = log(ERROR, msg)
fun <T> Class<T>.log(priority: Int, msg: String) {
    if (isLoggable(simpleName, priority)) {
        println(priority, simpleName, msg)
    }
}

fun <T> Class<T>.logError(msg: String, throwable: Throwable) = log(ERROR, msg, throwable)
fun <T> Class<T>.logWarn(msg: String, throwable: Throwable) = log(WARN, msg, throwable)
fun <T> Class<T>.log(priority: Int, msg: String, throwable: Throwable) {
    if (isLoggable(simpleName, priority)) {
        println(priority, simpleName, "$msg\n${throwable.stackTraceToString()}")
    }
}

