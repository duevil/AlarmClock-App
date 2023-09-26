package de.mk.alarmclock.ui.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.mk.alarmclock.R
import androidx.compose.material.icons.Icons as MaterialIcons

enum class Icons(
    private val vector: ImageVector?,
    @DrawableRes private val icon: Int?,
    @StringRes private val description: Int? = null,
) {
    Refresh(MaterialIcons.Rounded.Refresh),
    Settings(MaterialIcons.Rounded.Settings),
    ArrowBack(MaterialIcons.Rounded.ArrowBack),
    ArrowUp(MaterialIcons.Rounded.KeyboardArrowUp),
    ArrowDown(MaterialIcons.Rounded.KeyboardArrowDown),
    Add(MaterialIcons.Rounded.Add),
    Delete(MaterialIcons.Rounded.Delete),
    Keyboard(R.drawable.ic_keyboard),
    Time(R.drawable.ic_time),
    Sound(R.drawable.ic_sound),
    SoundSilent(R.drawable.ic_sound_silent),
    Play(R.drawable.ic_play),
    Stop(R.drawable.ic_stop),
    VolumeUp(R.drawable.ic_volume_up),
    VolumeDown(R.drawable.ic_volume_down),
    VolumeOff(R.drawable.ic_volume_off),
    Random(R.drawable.ic_random),
    Light(R.drawable.ic_light),
    CircuitBoard(R.drawable.ic_circuit_board),
    Language(R.drawable.ic_language),
    Help(R.drawable.ic_help),
    Reset(R.drawable.ic_reset),
    ;

    constructor(vector: ImageVector, description: Int? = null) : this(vector, null, description)
    constructor(icon: Int?, description: Int? = null) : this(null, icon, description)

    @Composable
    operator fun invoke(modifier: Modifier = Modifier) =
        vector?.let {
            Icon(
                imageVector = it,
                contentDescription = description?.let { stringResource(description) },
                modifier = modifier
            )
        } ?: icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = description?.let { stringResource(description) },
                modifier = modifier
            )
        } ?: throw IllegalArgumentException("No icon specified")
}