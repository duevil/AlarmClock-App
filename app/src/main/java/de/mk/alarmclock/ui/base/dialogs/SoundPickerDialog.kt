package de.mk.alarmclock.ui.base.dialogs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.mk.alarmclock.R
import de.mk.alarmclock.data.Sound
import de.mk.alarmclock.ui.base.Icon
import de.mk.alarmclock.ui.base.composeName
import de.mk.alarmclock.ui.base.items.TextItem

class SoundPickerDialog : FullScreenDialog() {
    @Composable
    operator fun invoke(sound: Sound, onSoundSelected: (Sound) -> Unit, sounds: Set<Sound>) {
        super.invoke(R.string.select_alarm_sound) {
            val state = rememberLazyListState(sound.id)
            LazyColumn(state = state) {
                items(sounds.sortedBy(Sound::id).toTypedArray(), key = Sound::id) {
                    TextItem(
                        headlineText = it.composeName(),
                        supportingText = "#${it.id}",
                        leadingContent = { it.Icon() },
                        trailingContent = {
                            val selected by remember { derivedStateOf { it == sound } }
                            RadioButton(
                                selected = selected,
                                onClick = { onSoundSelected(it) },
                            )
                        },
                        onClick = { onSoundSelected(it) },
                    )
                }
            }
        }
    }
}