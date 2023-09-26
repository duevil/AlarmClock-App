package de.mk.alarmclock.ui.screens

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.api.state
import de.mk.alarmclock.api.toMutable
import de.mk.alarmclock.data.Data
import de.mk.alarmclock.data.Light
import de.mk.alarmclock.ui.base.dialogs.TextInputDialog
import de.mk.alarmclock.ui.base.items.SectionTitleItem
import de.mk.alarmclock.ui.base.items.StandardItem
import de.mk.alarmclock.ui.base.items.TextItem

@Composable
fun MainViewModel.LightScreen() {
    val light = Data.LIGHT.get<Light>().toMutable()
    val lightState by light.state
    val dutyInputDialog = TextInputDialog()
    val durationInputDialog = TextInputDialog()

    dutyInputDialog(
        title = R.string.enter_light_duty,
        titleAddition = "${Light.dutyRange.first}-${Light.dutyRange.last}",
        onConfirm = { duty ->
            with(light) {
                value = value.copy(duty = duty.toInt())
                updateData()
            }
        },
        initial = lightState.duty.toString(),
        predicate = { it.toIntOrNull() in Light.dutyRange },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    durationInputDialog(
        title = R.string.enter_light_duration,
        titleAddition = "${Light.durationRange.first}-${Light.durationRange.last}",
        onConfirm = { duration ->
            with(light) {
                value = value.copy(duration = duration.toInt())
                updateData()
            }
        },
        initial = lightState.duration.toString(),
        predicate = { it.toIntOrNull() in Light.durationRange },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    SectionTitleItem(text = R.string.light_duty_title)
    TextItem(
        headlineTextR = R.string.light_duty,
        supportingText = "${lightState.duty * 100 / Light.dutyRange.last} %",
        trailingContent = {
            val checked = lightState.duty > Light.dutyRange.first
            val onClick = {
                with(light) {
                    value = value.copy(
                        duty = when {
                            checked -> Light.dutyRange.first
                            else -> Light.dutyRange.last
                        }
                    )
                    updateData()
                }
            }
            when {
                checked -> Button(onClick) { Text(stringResource(R.string.turn_light_off)) }
                else -> OutlinedButton(onClick) { Text(stringResource(R.string.turn_light_on)) }
            }
        },
        onClick = dutyInputDialog::show,
    )
    StandardItem {
        Slider(
            value = lightState.duty.toFloat(),
            onValueChange = { duty -> light.value = light.value.copy(duty = duty.toInt()) },
            onValueChangeFinished = { light.updateData() },
            valueRange = Light.dutyRange.first.toFloat()..Light.dutyRange.last.toFloat(),
        )
    }
    SectionTitleItem(text = R.string.light_duration_title)
    TextItem(
        headlineTextR = R.string.light_duration,
        supportingText = when {
            lightState.duration > Light.durationRange.first -> "${lightState.duration} min"
            else -> "∞"
        },
        onClick = durationInputDialog::show,
    )
    StandardItem {
        Slider(
            value = lightState.duration.toFloat(),
            onValueChange = { duration ->
                light.value = light.value.copy(duration = duration.toInt())
            },
            onValueChangeFinished = { light.updateData() },
            valueRange = Light.durationRange.first.toFloat()..Light.durationRange.last.toFloat(),
        )
    }
}