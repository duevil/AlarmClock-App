package de.mk.alarmclock.ui.base.dialogs

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.mk.alarmclock.R

@OptIn(ExperimentalComposeUiApi::class)
class TextInputDialog : DialogBase() {
    @Composable
    operator fun invoke(
        @StringRes title: Int,
        onConfirm: (String) -> Unit,
        onDismiss: () -> Unit = {},
        initial: String = "",
        placeholder: String = "",
        titleAddition: String? = null,
        predicate: (String) -> Boolean = { true },
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    ) {
        if (!show) return
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val toast = Toast.makeText(LocalContext.current, R.string.invalid_input, Toast.LENGTH_SHORT)
        var showToast by remember { mutableStateOf(false) }
        var value by remember {
            mutableStateOf(
                TextFieldValue(
                    text = initial,
                    selection = TextRange(0, initial.length)
                )
            )
        }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        LaunchedEffect(showToast) {
            if (showToast) {
                toast.show()
                showToast = false
            } else toast.cancel()
        }

        Dialog(onDismissRequest = {
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
                        text = stringResource(id = title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    titleAddition?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    TextField(
                        placeholder = { Text(placeholder) },
                        value = value,
                        onValueChange = { value = it.copy(it.text.trimStart()) },
                        isError = predicate(value.text).not(),
                        supportingText = {
                            Text(
                                text = stringResource(id = R.string.invalid_input),
                                modifier = Modifier.alpha(
                                    when {
                                        value.text.isEmpty() -> 0f
                                        predicate(value.text) -> 0f
                                        else -> 1f
                                    }
                                )
                            )
                        },
                        keyboardOptions = keyboardOptions,
                        keyboardActions = KeyboardActions(onDone = {
                            value.text.takeIf(predicate)?.let(onConfirm) ?: run {
                                showToast = true
                                return@KeyboardActions
                            }
                            show = false
                        }),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                show = false
                                onDismiss()
                            }
                        ) { Text(stringResource(android.R.string.cancel)) }
                        TextButton(
                            onClick = {
                                onConfirm(value.text.trimEnd())
                                show = false
                            },
                            enabled = predicate(value.text),
                        ) { Text(stringResource(android.R.string.ok)) }
                    }
                }
            }
        }
    }
}