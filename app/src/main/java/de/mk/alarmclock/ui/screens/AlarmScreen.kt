package de.mk.alarmclock.ui.screens

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.api.MutableRestfulData
import de.mk.alarmclock.api.state
import de.mk.alarmclock.api.toMutable
import de.mk.alarmclock.data.Alarm
import de.mk.alarmclock.data.CurrentDateTime
import de.mk.alarmclock.data.Data
import de.mk.alarmclock.data.Sound
import de.mk.alarmclock.ui.base.*
import de.mk.alarmclock.ui.base.dialogs.ConfirmDialog
import de.mk.alarmclock.ui.base.dialogs.DialogBase
import de.mk.alarmclock.ui.base.dialogs.SoundPickerDialog
import de.mk.alarmclock.ui.base.items.*
import java.util.*

@Composable
fun MainViewModel.AlarmScreen() {
    Column {
        Data.ALARM_ONE.get<Alarm>().toMutable().let { AlarmComposable(it) }
        Data.ALARM_TWO.get<Alarm>().toMutable().let { AlarmComposable(it) }
    }
}

@Composable
private fun MainViewModel.AlarmComposable(alarm: MutableRestfulData<Alarm>) {
    val alarmState by alarm.state
    val now by Data.CURRENT_DATE_TIME.get<CurrentDateTime>().state
    val duration by remember(alarmState) {
        derivedStateOf { with(alarmState) { now.instant.durationUntilNextAlarm } }
    }
    val sounds by Data.SOUNDS.get<Set<Sound>>().state
    val sound by remember(alarmState, sounds) {
        derivedStateOf { sounds.firstOrNull { it.id == alarmState.sound } }
    }
    val timePickerDialog = TimePickerDialog()
    val soundPickerDialog = SoundPickerDialog()
    val confirmIn8hDialog = ConfirmDialog()

    timePickerDialog(
        initialHour = alarmState.hour,
        initialMinute = alarmState.minute,
        onTimeSelected = { hour, minute ->
            with(alarm) {
                value = value.copy(hour = hour, minute = minute, toggle = true)
                updateData()
            }
        },
    )
    sounds.firstOrNull { it.id == alarmState.sound }?.let {
        soundPickerDialog(
            sound = it,
            onSoundSelected = { sound ->
                with(alarm) {
                    value = value.copy(sound = sound.id)
                    updateData()
                }
            },
            sounds = sounds,
        )
    }
    confirmIn8hDialog(
        title = R.string.alarm_confirm_in_8h_title,
        message = stringResource(
            R.string.alarm_confirm_in_8h_message, when (alarmState.id) {
                Alarm.ONE -> 1
                Alarm.TWO -> 2
                else -> error("Invalid alarm id ${alarmState.id}")
            }
        ),
        onConfirm = { setAlarmIn8h(alarm.value) },
    )
    SectionTitleItem(
        text = when (alarmState.id) {
            Alarm.ONE -> R.string.alarm_one
            Alarm.TWO -> R.string.alarm_two
            else -> error("Invalid alarm id ${alarmState.id}")
        },
    )
    Column {
        StandardItem(
            headlineContent = {
                Text(
                    text = "%02d:%02d".format(alarmState.hour, alarmState.minute),
                    style = MaterialTheme.typography.displayLarge
                )
            },
            supportingContent = {
                @Suppress("SimplifiableCallChain")
                Text(
                    text = Weekday.values()
                        .filter { alarmState.repeat and it.mask != 0 }
                        .takeIf { it.size < 7 }
                        ?.map { it.string }
                        ?.joinToString(", ")
                        ?.ifEmpty { stringResource(R.string.once) }
                        ?: stringResource(R.string.every_day)
                )
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = confirmIn8hDialog::show,
                        contentPadding = PaddingValues(vertical = 0.dp, horizontal = 12.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text(
                            text = "In ${pluralStringResource(R.plurals.hours, 8, 8)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    Switch(
                        checked = alarmState.toggle,
                        onCheckedChange = { toggle ->
                            with(alarm) {
                                value = value.copy(toggle = toggle)
                                updateData()
                            }
                        },
                    )
                }
            },
            onClick = {
                Log.i("AlarmScreen", "Clicked on alarm ${alarmState.id}")
                timePickerDialog.show()
            },
        )
        StandardItem {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Weekday.values().forEach { day ->
                    FilledIconToggleButton(
                        checked = alarmState.repeat and day.mask != 0,
                        onCheckedChange = {
                            with(alarm) {
                                value = value.copy(repeat = value.repeat xor day.mask)
                                updateData()
                            }
                        },
                        modifier = Modifier.size(32.dp),
                    ) { Text(day.string.take(1)) }
                }
            }
        }
        SmallTextItem(
            text = sound?.composeName() ?: stringResource(R.string.error_loading_sound),
            leadingContent = { sound.Icon() },
            onClick = soundPickerDialog::show,
        )
        SmallTextItem(
            text = when {
                alarmState.toggle -> "Alarm in ${duration.durationString()}"
                else -> stringResource(R.string.alarm_disabled)
            },
            leadingContent = { Icons.Time() },
            onClick = timePickerDialog::show,
        )
    }
}

private enum class Weekday(@StringRes private val str: Int, bit: Int) {
    Monday(R.string.weekday_monday, 1),
    Tuesday(R.string.weekday_tuesday, 2),
    Wednesday(R.string.weekday_wednesday, 3),
    Thursday(R.string.weekday_thursday, 4),
    Friday(R.string.weekday_friday, 5),
    Saturday(R.string.weekday_saturday, 6),
    Sunday(R.string.weekday_sunday, 0),
    ;

    val string @Composable get() = stringResource(str)
    val mask = 1 shl bit
}

@OptIn(ExperimentalMaterial3Api::class)
private class TimePickerDialog : DialogBase() {
    @Composable
    operator fun invoke(
        initialHour: Int,
        initialMinute: Int,
        onTimeSelected: (hour: Int, minute: Int) -> Unit,
    ) {
        val timePickerState = rememberTimePickerState(initialHour, initialMinute)
        var keyboardInput by remember { mutableStateOf(false) }

        if (!show) return
        Dialog({ show = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.timer_picker_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(16.dp))
                    if (keyboardInput) {
                        TimeInput(
                            state = timePickerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                        )
                    } else {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            onClick = { keyboardInput = !keyboardInput },
                            content = { if (keyboardInput) Icons.Time() else Icons.Keyboard() }
                        )
                        Row {
                            TextButton(
                                onClick = { show = false },
                                content = { Text(stringResource(android.R.string.cancel)) }
                            )
                            TextButton(
                                onClick = {
                                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                                    show = false
                                },
                                content = { Text(stringResource(android.R.string.ok)) },
                            )
                        }
                    }
                }
            }
        }
    }
}