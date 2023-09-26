package de.mk.alarmclock.ui.base.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.mk.alarmclock.ui.base.Icons
import de.mk.alarmclock.ui.base.PaddingBox

@OptIn(ExperimentalMaterial3Api::class)
open class FullScreenDialog(private val onDismiss: () -> Unit = {}) : DialogBase() {
    @Composable
    operator fun invoke(
        @StringRes title: Int,
        content: @Composable () -> Unit,
    ) {
        if (!show) return
        Dialog(
            onDismissRequest = {
                show = false
                onDismiss()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(title)) },
                        navigationIcon = {
                            IconButton({
                                show = false
                                onDismiss()
                            }) { Icons.ArrowBack() }
                        }
                    )
                },
            ) {
                PaddingBox {
                    Box(
                        Modifier
                            .padding(it)
                            .fillMaxSize(),
                    ) { content() }
                }
            }
        }
    }
}