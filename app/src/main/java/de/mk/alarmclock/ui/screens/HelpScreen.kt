package de.mk.alarmclock.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.mk.alarmclock.R
import de.mk.alarmclock.ui.base.items.SectionTitleItem
import de.mk.alarmclock.ui.base.items.SmallTextItem

@Composable
fun HelpScreen() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        SmallTextItem(stringResource(R.string.help_overview))
        helpItems.forEach { it() }
    }
}

private val helpItems = listOf(
    HelpItem(
        R.string.help_basic_functions_title,
        R.string.help_basic_functions
    ),
    HelpItem(
        R.string.help_data_refreshing_title,
        R.string.help_data_refreshing
    ),
    HelpItem(
        R.string.help_home_screen_title,
        R.string.help_home_screen
    ),
    HelpItem(
        R.string.help_alarm_screen_title,
        R.string.help_alarm_screen
    ),
    HelpItem(
        R.string.help_light_screen_title,
        R.string.help_light_screen
    ),
    HelpItem(
        R.string.help_player_screen_title,
        R.string.help_player_screen
    ),
    HelpItem(
        R.string.help_sounds_screen_title,
        R.string.help_sounds_screen
    ),
)

private data class HelpItem(@StringRes val title: Int, @StringRes val text: Int) {
    @Composable
    operator fun invoke() {
        SectionTitleItem(title)
        SmallTextItem(stringResource(text))
    }
}