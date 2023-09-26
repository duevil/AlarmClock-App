package de.mk.alarmclock.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import de.mk.alarmclock.R
import java.time.Duration

@Composable
fun Duration.durationString() = listOfNotNull(
    toDaysPart().takeIf { it > 0 }?.toInt()?.let { pluralStringResource(R.plurals.days, it, it) },
    toHoursPart().takeIf { it > 0 }?.let { pluralStringResource(R.plurals.hours, it, it) },
    toMinutesPart().let { pluralStringResource(R.plurals.minutes, it, it) },
    toSecondsPart().let { pluralStringResource(R.plurals.seconds, it, it) },
).joinToString(", ")