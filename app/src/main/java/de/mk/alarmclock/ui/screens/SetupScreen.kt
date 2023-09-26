package de.mk.alarmclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.ui.base.PaddingBox
import de.mk.alarmclock.ui.base.items.HeadlineItem
import de.mk.alarmclock.ui.base.items.SmallTextItem

@Composable
fun MainViewModel.SetupScreen() {
    val setupDone by settings.setupDone.stateFlow().collectAsState()
    val onFinish = {
        settings.setupDone.update(true).invokeOnCompletion { refresh() }
        Unit // needed as otherwise the compiler complains about a non-Unit return type
    }

    if (setupDone) return
    Dialog(
        onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PaddingBox {
            Column(
                modifier = Modifier
                    .padding()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Spacer(Modifier.weight(.1f))
                HeadlineItem(R.string.setup)
                SmallTextItem(stringResource(R.string.setup_message))
                SettingsScreen(false)
                Spacer(Modifier.weight(.6f))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp)
                ) { Button(onFinish) { Text(stringResource(R.string.setup_continue)) } }
                Spacer(Modifier.weight(.3f))
            }
        }
    }
}