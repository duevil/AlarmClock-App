package de.mk.alarmclock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.api.state
import de.mk.alarmclock.api.toMutable
import de.mk.alarmclock.data.Data
import de.mk.alarmclock.data.Player
import de.mk.alarmclock.data.Sound
import de.mk.alarmclock.ui.base.Icon
import de.mk.alarmclock.ui.base.Icons
import de.mk.alarmclock.ui.base.composeName
import de.mk.alarmclock.ui.base.dialogs.SoundPickerDialog
import de.mk.alarmclock.ui.base.dialogs.TextInputDialog
import de.mk.alarmclock.ui.base.items.SectionTitleItem
import de.mk.alarmclock.ui.base.items.StandardItem
import de.mk.alarmclock.ui.base.items.TextItem

@Composable
fun MainViewModel.PlayerScreen() {
    val soundPickerDialog = SoundPickerDialog()
    val volumeInputDialog = TextInputDialog()
    val player = Data.PLAYER.get<Player>().toMutable()
    val playerState by player.state
    val volume = playerState.volume
    val sounds by Data.SOUNDS.get<Set<Sound>>().state
    var selected by remember { mutableStateOf(sounds.elementAtOrNull(0)) }

    selected?.let { sound ->
        soundPickerDialog(
            sound = sound,
            onSoundSelected = { selected = it },
            sounds = sounds,
        )
    }
    volumeInputDialog(
        title = R.string.input_player_volume,
        titleAddition = "${Player.volumeRange.first}-${Player.volumeRange.last}",
        onConfirm = {
            with(player) {
                value = value.copy(volume = it.toInt())
                updateData()
            }
        },
        initial = volume.toString(),
        predicate = { it.toIntOrNull() in Player.volumeRange },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    SectionTitleItem(R.string.player_play)
    TextItem(
        headlineTextR = R.string.selected_sound,
        supportingText = selected?.composeName() ?: stringResource(R.string.error_loading_sound),
        leadingContent = { selected.Icon(Modifier.size(32.dp)) },
        onClick = { soundPickerDialog.show() },
    )
    StandardItem(Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = { selected?.let { playSound(it) } },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icons.Play()
                    Spacer(Modifier.size(8.dp))
                    Text("Play")
                    Spacer(Modifier.size(8.dp))
                }
            }
            Spacer(Modifier.size(24.dp))
            Button(
                onClick = { stopPlayback() },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icons.Stop()
                    Spacer(Modifier.size(8.dp))
                    Text("Stop")
                    Spacer(Modifier.size(8.dp))
                }
            }
        }
    }
    SectionTitleItem(R.string.player_volume)
    TextItem(
        headlineTextR = R.string.volume,
        supportingText = "${volume * 100 / Player.volumeRange.last} %",
        leadingContent = {
            (when {
                volume == 0 -> Icons.VolumeOff
                volume < Player.volumeRange.last / 2 -> Icons.VolumeDown
                else -> Icons.VolumeUp
            })(Modifier.size(32.dp))
        },
        onClick = volumeInputDialog::show,
    )
    StandardItem {
        Slider(
            value = volume.toFloat(),
            valueRange = Player.volumeRange.first.toFloat()..Player.volumeRange.last.toFloat(),
            onValueChange = { player.value = player.value.copy(volume = it.toInt()) },
            onValueChangeFinished = { player.updateData() },
        )
    }
}