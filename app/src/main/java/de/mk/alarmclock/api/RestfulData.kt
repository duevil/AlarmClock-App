package de.mk.alarmclock.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response

interface RestfulData<T> {
    val value: T
    val stateFlow: StateFlow<T>
    suspend fun fetch(apiService: ApiService): RestfulCall.Result<T>
}

interface MutableRestfulData<T> : RestfulData<T> {
    override var value: T
    suspend fun update(apiService: ApiService): RestfulCall.Result<Void>
}

private class RestfulDataImpl<T>(
    initial: T,
    fetchCall: suspend (ApiService) -> Response<T>,
    updateCall: (suspend (ApiService, T) -> Response<Void>)? = null,
) : RestfulData<T>, MutableRestfulData<T> {
    private val data = MutableStateFlow(initial)
    private val _fetchCall = RestfulCall(RestfulCall.Type.FETCH_DATA, fetchCall)
    private val _updateCall = updateCall?.let { c ->
        RestfulCall(RestfulCall.Type.UPDATE_DATA) { c(it, data.value) }
    }
    val fetchId get() = _fetchCall.id
    val updateId get() = _updateCall?.id

    override val stateFlow get() = data.asStateFlow()
    override var value: T
        get() = data.value
        set(value) {
            data.value = value
        }

    override suspend fun fetch(apiService: ApiService): RestfulCall.Result<T> =
        _fetchCall(apiService).also { result -> result.value?.let { value = it } }

    override suspend fun update(apiService: ApiService) =
        _updateCall
            ?.invoke(apiService)
            ?: throw UnsupportedOperationException("update not supported")
}

fun <T> RestfulData(
    initial: T,
    fetchCall: suspend (ApiService) -> Response<T>,
): RestfulData<T> = RestfulDataImpl(initial, fetchCall)

fun <T> MutableRestfulData(
    initial: T,
    fetchCall: suspend (ApiService) -> Response<T>,
    updateCall: suspend (ApiService, T) -> Response<Void>,
): MutableRestfulData<T> = RestfulDataImpl(initial, fetchCall, updateCall)

val RestfulData<*>.fetchId get() = (this as? RestfulDataImpl<*>)?.fetchId ?: -1
val MutableRestfulData<*>.updateId get() = (this as? RestfulDataImpl<*>)?.updateId ?: -1
val <T> RestfulData<T>.state @Composable get() = stateFlow.collectAsState()
fun <T> RestfulData<T>.toMutable() = this as? MutableRestfulData<T>
    ?: throw UnsupportedOperationException("not mutable")
