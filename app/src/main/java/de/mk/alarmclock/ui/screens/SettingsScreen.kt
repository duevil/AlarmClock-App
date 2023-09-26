package de.mk.alarmclock.ui.screens

import android.app.Application
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.mk.alarmclock.BuildConfig
import de.mk.alarmclock.Language
import de.mk.alarmclock.Language.Companion.language
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.openGitHubRepo
import de.mk.alarmclock.settings.Setting
import de.mk.alarmclock.ui.base.Icons
import de.mk.alarmclock.ui.base.dialogs.ConfirmDialog
import de.mk.alarmclock.ui.base.dialogs.FullScreenDialog
import de.mk.alarmclock.ui.base.dialogs.TextInputDialog
import de.mk.alarmclock.ui.base.items.SectionTitleItem
import de.mk.alarmclock.ui.base.items.TextItem

val settingsDialog = FullScreenDialog()

@Composable
fun MainViewModel.SettingsScreen(showAppSection: Boolean = true) {
    val helpDialog = FullScreenDialog()
    val languageSelectionDialog = FullScreenDialog()
    val resetConfirmDialog = ConfirmDialog()
    val localeListCompat = getLocalListCompatState()

    helpDialog(R.string.help) { HelpScreen() }
    languageSelectionDialog(R.string.settings_language) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Language.values().forEach { language ->
                val onConfirm: () -> Unit = {
                    setLocal(language)
                    languageSelectionDialog.dismiss()
                    settingsDialog.dismiss()
                }
                TextItem(
                    headlineText = language.text,
                    trailingContent = {
                        RadioButton(
                            selected = localeListCompat.value.language == language,
                            onClick = onConfirm
                        )
                    },
                    onClick = onConfirm,
                )
            }
        }
    }
    resetConfirmDialog(
        title = R.string.settings_reset,
        message = stringResource(R.string.settings_reset_message),
        onConfirm = {
            settingsDialog.dismiss()
            settings.host.reset()
            settings.port.reset()
            settings.setupDone.reset()
            setLocal(Language.SYSTEM)
        }
    )

    Column(if (showAppSection) Modifier.verticalScroll(rememberScrollState()) else Modifier) {
        SectionTitleItem(R.string.settings_connection)
        SettingsItem(
            setting = settings.host,
            title = R.string.settings_host,
            strOp = object : StringOperator<String> {
                override fun toString(value: String) = value
                override fun fromString(value: String) = value
            },
            placeholder = settings.host.default,
            predicate = Regex("^((localhost)|((?:\\d{1,3}\\.){3}\\d{1,3}))$")::matches
        )
        SettingsItem(
            setting = settings.port,
            title = R.string.settings_port,
            strOp = object : StringOperator<Int> {
                override fun toString(value: Int) = value.toString()
                override fun fromString(value: String) = value.toIntOrNull() ?: 0
            },
            placeholder = settings.port.default.toString(),
            predicate = { (it.toIntOrNull() ?: Int.MAX_VALUE) < Short.MAX_VALUE }
        )
        SectionTitleItem(R.string.settings_language)
        TextItem(
            headlineTextR = R.string.settings_language,
            supportingText = stringResource(
                R.string.current_language,
                localeListCompat.value.language.text
            ),
            leadingContent = { Icons.Language() },
            onClick = languageSelectionDialog::show,
        )
        if (showAppSection) {
            SectionTitleItem(R.string.settings_app)
            TextItem(
                headlineTextR = R.string.help,
                supportingText = stringResource(R.string.settings_help_support),
                leadingContent = { Icons.Help() },
                onClick = helpDialog::show,
            )
            TextItem(
                headlineTextR = R.string.settings_reset,
                supportingText = stringResource(R.string.settings_reset_support),
                leadingContent = { Icons.Reset() },
                onClick = resetConfirmDialog::show,
            )
            Divider(Modifier.padding(16.dp))
            TextItem(
                headlineTextR = R.string.settings_github,
                supportingText = stringResource(R.string.settings_github_support),
                onClick = { getApplication<Application>().openGitHubRepo() }
            )
            TextItem(
                headlineTextR = R.string.about,
                supportingText = """
                    App Version: ${BuildConfig.VERSION_NAME}
                    Build: ${BuildConfig.BUILD_TYPE}
                    Model: ${Build.MODEL}
                    Android Version: ${Build.VERSION.RELEASE}

                    © 2023 Malte Kasolowsky
                    """.trimIndent(),
                onDoubleClick = {
                    Toast.makeText(
                        getApplication(),
                        "Easter Egg \uD83D\uDC23✨",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private interface StringOperator<T> {
    fun toString(value: T): String
    fun fromString(value: String): T
}

@Composable
private fun <T> MainViewModel.SettingsItem(
    setting: Setting<T>,
    @StringRes title: Int,
    strOp: StringOperator<T>,
    placeholder: String = "",
    predicate: (String) -> Boolean = { true },
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val text by setting.stateFlow().collectAsState()
    val dialog = TextInputDialog()
    dialog(
        title = title,
        onConfirm = { setting.update(strOp.fromString(it)) },
        initial = strOp.toString(text),
        placeholder = placeholder,
        predicate = predicate,
    )
    TextItem(
        headlineTextR = title,
        supportingText = strOp.toString(text),
        leadingContent = leadingContent,
        onClick = { dialog.show() },
    )
}
