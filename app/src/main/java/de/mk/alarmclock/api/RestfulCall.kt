package de.mk.alarmclock.api

import de.mk.alarmclock.logError
import de.mk.alarmclock.logInfo
import de.mk.alarmclock.logWarn
import retrofit2.Response

open class RestfulCall<T>(
    private val type: Type,
    private val call: suspend (ApiService) -> Response<T>,
) {
    @get:Synchronized
    val id = nextId++

    suspend operator fun invoke(apiService: ApiService): Result<T> {
        javaClass.logInfo("RestfulCall start")
        return try {
            val response = call(apiService)
            javaClass.logInfo("RestfulCall response: $response")
            if (response.isSuccessful) {
                val body = response.body()
                javaClass.logInfo("RestfulCall success: $body")
                Result(type, id, true, body)
            } else {
                val responseError = response.errorBody()
                    ?.string()
                    ?.takeUnless(String::isBlank)
                    ?: response.message()
                javaClass.logWarn("RestfulCall failed: $responseError")
                Result(type, id, false, errorMsg = responseError)
            }
        } catch (e: Exception) {
            javaClass.logError("RestfulCall failed", e)
            Result(type, id, false, errorMsg = e.message ?: "Unknown error")
        }
    }

    companion object {
        @get:Synchronized
        @set:Synchronized
        private var nextId = 0
    }

    data class Result<T>(
        val type: Type,
        val id: Int,
        val success: Boolean,
        val value: T? = null,
        val errorMsg: String? = null,
    )

    enum class Type { FETCH_DATA, UPDATE_DATA, RUN_TASK }
}