package de.mk.alarmclock.ui.base.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

class ConfirmDialog : DialogBase() {
    @Composable
    operator fun invoke(
        @StringRes title: Int,
        message: String,
        onDismiss: () -> Unit = {},
        onConfirm: () -> Unit,
    ) {
        if (!show) return
        Dialog({
            show = false
            onDismiss()
        }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 20.dp,
                            bottom = 18.dp,
                            start = 24.dp,
                            end = 24.dp
                        )
                ) {
                    Text(
                        text = stringResource(title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = message,
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.End) {
                        TextButton({
                            show = false
                            onDismiss()
                        }) { Text(stringResource(android.R.string.cancel)) }
                        TextButton({
                            show = false
                            onConfirm()
                            onDismiss()
                        }) { Text(stringResource(android.R.string.ok)) }
                    }
                }
            }
        }
    }
}