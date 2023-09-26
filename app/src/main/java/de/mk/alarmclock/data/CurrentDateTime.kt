package de.mk.alarmclock.data

import java.time.Instant

data class CurrentDateTime(
    val value: Long,
) {
    val instant: Instant get() = Instant.ofEpochSecond(value)
}
