package de.mk.alarmclock.data

import com.google.gson.annotations.SerializedName
import java.time.Duration
import java.time.Instant
import java.time.temporal.Temporal

data class Alarm(
    var id: Int,
    var hour: Int,
    var minute: Int,
    var repeat: Int,
    var toggle: Boolean,
    var sound: Int,
    @SerializedName("nextDateTime")
    var nextDateTimeUnix: Long,
) {
    init {
        require(id in ONE..TWO) { "Alarm id must be 1 or 2" }
    }

    companion object {
        const val ONE = 1
        const val TWO = 2
    }

    val nextDateTime: Instant
        get() = nextDateTimeUnix.let { Instant.ofEpochSecond(it) }
    val Temporal.durationUntilNextAlarm: Duration
        get() = Duration.between(this, nextDateTime)
}
