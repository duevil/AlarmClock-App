package de.mk.alarmclock.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.api.state
import de.mk.alarmclock.data.Alarm
import de.mk.alarmclock.data.CurrentDateTime
import de.mk.alarmclock.data.Data
import de.mk.alarmclock.data.LightSensor
import de.mk.alarmclock.ui.base.Icons
import de.mk.alarmclock.ui.base.durationString
import de.mk.alarmclock.ui.base.items.SectionTitleItem
import de.mk.alarmclock.ui.base.items.SmallTextItem
import de.mk.alarmclock.ui.base.items.TextItem
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MainViewModel.HomeScreen() {
    val currentDateTime by Data.CURRENT_DATE_TIME.get<CurrentDateTime>().state
    val alarmOne by Data.ALARM_ONE.get<Alarm>().state
    val alarmTwo by Data.ALARM_TWO.get<Alarm>().state
    val lightValue by Data.LIGHT_SENSOR.get<LightSensor>().state
    val onTime by Data.ON_TIME.get<Long>().state
    val timeToA1 by remember {
        derivedStateOf { with(alarmOne) { currentDateTime.instant.durationUntilNextAlarm } }
    }
    val timeToA2 by remember {
        derivedStateOf { with(alarmTwo) { currentDateTime.instant.durationUntilNextAlarm } }
    }

    SmallTextItem(
        text = stringResource(R.string.welcome_message),
        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
    )
    Spacer(Modifier.size(16.dp))
    TextItem(
        headlineText = stringResource(R.string.current_time),
        supportingText = currentDateTime.instant.getString(fullLocalDateTimeFormatter),
        leadingContent = { Icons.Time(Modifier.size(32.dp)) },
    )
    SectionTitleItem(R.string.alarm_info)
    AlarmItem(alarmOne, timeToA1)
    AlarmItem(alarmTwo, timeToA2)
    SectionTitleItem(R.string.other_info)
    TextItem(
        headlineText = stringResource(R.string.light_value),
        supportingText = "%.2f lx".format(lightValue.value),
        leadingContent = { Icons.Light(Modifier.size(32.dp)) },
    )
    TextItem(
        headlineText = stringResource(R.string.uptime),
        supportingText = "${stringResource(R.string.running_since)} ${
            Duration.ofSeconds(onTime).durationString()
        }",
        leadingContent = { Icons.CircuitBoard(Modifier.size(32.dp)) },
    )
}

@Composable
private fun AlarmItem(alarm: Alarm, timeTo: Duration) {
    TextItem(
        headlineText = when {
            alarm.toggle -> alarm.nextDateTime.getString(patternDateTimeFormatter)
            else -> stringResource(R.string.alarm_disabled)
        },
        overlineText = stringResource(
            when (alarm.id) {
                1 -> R.string.alarm_one
                2 -> R.string.alarm_two
                else -> error("Invalid alarm id ${alarm.id}")
            }
        ),
        supportingText = if (alarm.toggle) "Alarm in ${timeTo.durationString()}" else null,
    )
}

private val fullLocalDateTimeFormatter: DateTimeFormatter
    get() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
private val patternDateTimeFormatter: DateTimeFormatter
    @Composable get() = DateTimeFormatter.ofPattern(stringResource(R.string.alarm_datetime_pattern))

private fun Instant.getString(dateTimeFormatter: DateTimeFormatter) =
    LocalDateTime.ofInstant(this, ZoneId.systemDefault()).format(
        dateTimeFormatter.withZone(ZoneOffset.systemDefault())
    )