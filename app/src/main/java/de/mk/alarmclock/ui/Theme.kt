package de.mk.alarmclock.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun AlarmClockControllerTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = when (isSystemInDarkTheme()) {
        true -> dynamicDarkColorScheme(LocalContext.current)
        false -> dynamicLightColorScheme(LocalContext.current)
    },
    content = content
)