package de.mk.alarmclock

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.mk.alarmclock.api.ApiService
import de.mk.alarmclock.api.MutableRestfulData
import de.mk.alarmclock.api.RestfulCall
import de.mk.alarmclock.api.RestfulData
import de.mk.alarmclock.api.fetchId
import de.mk.alarmclock.api.updateId
import de.mk.alarmclock.data.Alarm
import de.mk.alarmclock.data.CurrentDateTime
import de.mk.alarmclock.data.Data
import de.mk.alarmclock.data.Light
import de.mk.alarmclock.data.LightSensor
import de.mk.alarmclock.data.Player
import de.mk.alarmclock.data.Sound
import de.mk.alarmclock.settings.Setting
import de.mk.alarmclock.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {
    val settings = Settings(getApplication())
    var onRequestFailed: suspend (
        type: RestfulCall.Type,
        msg: String,
        retry: (() -> Unit)?,
    ) -> Unit = { _, _, _ -> }
    val needToRefresh by derivedStateOf { refreshTimer > 600 && callsRunning.value == 0 }
    private var refreshTimer by mutableIntStateOf(600)
    private var callsRunning = MutableStateFlow(0)
    private val data = mapOf(
        Data.ON_TIME to RestfulData(
            initial = 0,
            fetchCall = ApiService::getOnTime,
        ),
        Data.CURRENT_DATE_TIME to RestfulData(
            initial = CurrentDateTime(0),
            fetchCall = ApiService::getCurrentDateTime,
        ),
        Data.ALARM_ONE to MutableRestfulData(
            initial = Alarm(Alarm.ONE, 0, 0, 0, false, 0, 0),
            fetchCall = { api -> api.getAlarm(Alarm.ONE) },
            updateCall = { api: ApiService, value: Alarm -> api.setAlarm(value) },
        ),
        Data.ALARM_TWO to MutableRestfulData(
            initial = Alarm(Alarm.TWO, 0, 0, 0, false, 0, 0),
            fetchCall = { api -> api.getAlarm(Alarm.TWO) },
            updateCall = { api: ApiService, value: Alarm -> api.setAlarm(value) },
        ),
        Data.LIGHT to MutableRestfulData(
            initial = Light(0, 0),
            fetchCall = { api -> api.getLight() },
            updateCall = { api: ApiService, value: Light -> api.setLight(value) },
        ),
        Data.PLAYER to MutableRestfulData(
            initial = Player(0),
            fetchCall = { api -> api.getPlayer() },
            updateCall = { api: ApiService, value: Player -> api.setPlayer(value) },
        ),
        Data.LIGHT_SENSOR to RestfulData(
            initial = LightSensor(0.0),
            fetchCall = { api -> api.getLightSensor() },
        ),
        Data.SOUNDS to MutableRestfulData(
            initial = emptySet(),
            fetchCall = { api -> api.getSounds() },
            updateCall = { api: ApiService, value: Set<Sound> -> api.setSounds(value) },
        ),
    )
    private val apiService = settings.host.flow
        .map(HttpUrl.Builder()::host)
        .combine(settings.port.flow, HttpUrl.Builder::port)
        .map { it.scheme("http") }
        .map(HttpUrl.Builder::build)
        .map(ApiService::create)
    private val localListCompat = MutableStateFlow(Language.appLocalListCompat)

    init {
        viewModelScope.launch {
            while (true) {
                delay(100)
                refreshTimer++
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(LivecycleObserver())
    }

    fun refresh() {
        javaClass.logInfo("Fetching all data")
        @Suppress("kotlin:S6310")
        viewModelScope.launch(Dispatchers.IO) {
            if (!settings.setupDone.flow.first()) {
                javaClass.logWarn("Setup not finished, aborting data fetching")
                return@launch
            }
            if (callsRunning.value != 0) {
                javaClass.logWarn("Calls running, aborting data fetching")
                return@launch
            }
            var previous: Int? = null
            for (it in data.values) {
                previous?.removeCallRunning()
                previous = it.fetchId
                /* runFetching can not be used here because it remove a call running
                 * before the next call is started. But here we want to remove the call
                 * running after the next call is started to prevent the callsRunning
                 * state to be 0 for a short time. */
                javaClass.logInfo("Fetching data [$previous]")
                previous.addCallRunning()
                val result = it.fetch(apiService.first())
                if (!result.success) {
                    previous.removeCallRunning()
                    javaClass.logWarn("Failed to fetch data [$result]")
                    onRequestFailed(result.type, result.errorMsg!!, null)
                    return@launch
                }
            }
            previous?.removeCallRunning()
            refreshTimer = 0
        }
    }

    @Composable
    fun callsRunning() = callsRunning.collectAsState().value > 0

    @Suppress("UNCHECKED_CAST")
    fun <T> Data.get() = data[this] as? RestfulData<T> ?: error("No mapping for $this found")

    fun <T> MutableRestfulData<T>.updateData() {
        fun onFail() {
            viewModelScope.launch {
                javaClass.logInfo("Retrying fetching data [$fetchId]")
                runFetching(apiService.first(), ::onFail)
            }
        }

        if (callsRunning.value != 0) {
            javaClass.logWarn("Calls running, aborting data update")
            return
        }

        javaClass.logInfo("Updating and fetching data [$updateId/$fetchId]")
        viewModelScope.launch {
            val apiService = apiService.first()
            javaClass.logInfo("Updating data [$updateId]")
            updateId.addCallRunning()
            val result = update(apiService)
            updateId.removeCallRunning()
            if (!result.success) {
                javaClass.logWarn("Failed to update data [$result]")
                onRequestFailed(result.type, result.errorMsg!!) {
                    javaClass.logInfo("Retrying to update data [u$updateId/$fetchId]")
                    updateData()
                }
            } else if (runFetching(apiService, ::onFail)) dataUpdateSuccessfulToast()
        }
    }

    fun <T> Setting<T>.stateFlow() = flow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000L),
        default
    )

    fun <T> Setting<T>.update(value: T): Job {
        javaClass.logInfo("Updating setting [${this}] to [${value}]")
        return viewModelScope.launch { set(value) }
    }

    fun <T> Setting<T>.reset() {
        javaClass.logInfo("Resetting setting [${this}]")
        viewModelScope.launch { remove() }
    }

    fun setAlarmIn8h(alarm: Alarm) {
        val data = when (alarm.id) {
            Alarm.ONE -> Data.ALARM_ONE
            Alarm.TWO -> Data.ALARM_TWO
            else -> error("Unknown alarm id ${alarm.id}")
        }.get<Alarm>()

        fun onFail() {
            viewModelScope.launch {
                javaClass.logInfo("Retrying fetching alarm data [${data.fetchId}]")
                data.runFetching(apiService.first(), ::onFail)
            }
        }

        val call = RestfulCall(RestfulCall.Type.UPDATE_DATA) { api -> api.setAlarmIn8h(alarm.id) }
        javaClass.logInfo("Setting alarm in 8h [${call.id}]")
        viewModelScope.launch {
            if (call.run().success && data.runFetching(apiService.first(), ::onFail)) {
                dataUpdateSuccessfulToast()
            }
        }
    }

    fun addSound(sound: Sound) {
        val call = RestfulCall(RestfulCall.Type.UPDATE_DATA) { api -> api.addSound(sound) }
        javaClass.logInfo("Adding sound [${call.id}]")
        viewModelScope.launch {
            if (call.run().success && fetchSounds()) dataUpdateSuccessfulToast()
        }
    }

    fun setSound(sound: Sound) {
        val call = RestfulCall(RestfulCall.Type.UPDATE_DATA) { api -> api.setSound(sound) }
        javaClass.logInfo("Updating sound #${sound.id} [${call.id}]")
        viewModelScope.launch {
            if (call.run().success && fetchSounds()) dataUpdateSuccessfulToast()
        }
    }

    fun deleteSound(sound: Sound) {
        val call = RestfulCall(RestfulCall.Type.UPDATE_DATA) { api -> api.deleteSound(sound.id) }
        javaClass.logInfo("Deleting sound #${sound.id} [${call.id}]")
        viewModelScope.launch {
            if (call.run().success && fetchSounds()) dataUpdateSuccessfulToast()
        }
    }

    fun playSound(sound: Sound) {
        val call = RestfulCall(RestfulCall.Type.RUN_TASK) { it.play(sound.id) }
        javaClass.logInfo("Playing sound #${sound.id} [${call.id}]")
        viewModelScope.launch { if (call.run().success) taskRunSuccessfulToast() }
    }

    fun stopPlayback() {
        val call = RestfulCall(RestfulCall.Type.RUN_TASK, ApiService::stop)
        javaClass.logInfo("Stopping playback [${call.id}]")
        viewModelScope.launch { if (call.run().success) taskRunSuccessfulToast() }
    }

    @Composable
    fun getLocalListCompatState() = localListCompat.collectAsState()

    fun setLocal(language: Language) {
        localListCompat.value = when (language) {
            Language.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(language.code)
        }
    }

    private suspend fun <T> RestfulData<T>.runFetching(
        api: ApiService,
        onFail: (() -> Unit)? = null,
    ): Boolean {
        javaClass.logInfo("Fetching data [$fetchId]")
        fetchId.addCallRunning()
        val result = fetch(api)
        fetchId.removeCallRunning()
        if (!result.success) {
            javaClass.logWarn("Failed to fetch data [$result]")
            onRequestFailed(result.type, result.errorMsg!!, onFail)
        }
        return result.success
    }

    private suspend fun <T> RestfulCall<T>.run(): RestfulCall.Result<T> {
        javaClass.logInfo("Running call [${this.id}]")
        id.addCallRunning()
        val result = invoke(apiService.first())
        id.removeCallRunning()
        if (!result.success) {
            javaClass.logWarn("Failed to run call [$result]")
            onRequestFailed(result.type, result.errorMsg!!) {
                javaClass.logInfo("Retrying to run call [${this.id}]")
                viewModelScope.launch { run() }
            }
        }
        return result
    }

    private suspend fun fetchSounds(): Boolean {
        val restfulData = Data.SOUNDS.get<Set<Sound>>()

        fun onFail() {
            viewModelScope.launch {
                javaClass.logInfo("Retrying fetching sounds [${restfulData.fetchId}]")
                restfulData.runFetching(apiService.first(), ::onFail)
            }
        }

        return restfulData.runFetching(apiService.first(), ::onFail)
    }

    private fun Int.addCallRunning() {
        callsRunning.value = callsRunning.value or 1 shl this
    }

    private fun Int.removeCallRunning() {
        callsRunning.value = callsRunning.value and (1 shl this).inv()
    }

    private fun dataUpdateSuccessfulToast() {
        Toast.makeText(
            getApplication(),
            R.string.update_successful,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun taskRunSuccessfulToast() {
        Toast.makeText(
            getApplication(),
            R.string.task_run_successful,
            Toast.LENGTH_SHORT
        ).show()
    }

    private inner class LivecycleObserver : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            refresh()
            viewModelScope.launch {
                Language.appLocalListCompat.let {
                    if (it != localListCompat.value) {
                        localListCompat.value = it
                    }
                }
            }
        }
    }
}

