package de.mk.alarmclock.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
inline fun PaddingBox(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = 4.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(padding)
            .fillMaxSize(),
    ) { content() }
}