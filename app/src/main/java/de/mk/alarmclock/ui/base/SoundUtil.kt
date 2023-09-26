package de.mk.alarmclock.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.mk.alarmclock.R
import de.mk.alarmclock.data.Sound

@Composable
fun Sound?.Icon(modifier: Modifier = Modifier) = (when (this?.id) {
    Sound.RANDOM -> Icons.Random
    Sound.SILENT -> Icons.SoundSilent
    null -> Icons.SoundSilent
    else -> Icons.Sound
})(modifier)

@Composable
fun Sound.composeName() = when (this.id) {
    Sound.RANDOM -> stringResource(R.string.sound_random)
    Sound.SILENT -> stringResource(R.string.sound_silent)
    else -> name
}