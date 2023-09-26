package de.mk.alarmclock.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.ui.screens.AlarmScreen
import de.mk.alarmclock.ui.screens.HomeScreen
import de.mk.alarmclock.ui.screens.LightScreen
import de.mk.alarmclock.ui.screens.PlayerScreen
import de.mk.alarmclock.ui.screens.SoundsScreen

enum class Navigation(
    val screen: @Composable (MainViewModel.() -> Unit),
    @StringRes val title: Int,
    @StringRes private val label: Int,
    @DrawableRes private val iconFilled: Int,
    @DrawableRes private val iconOutlined: Int,
) {
    Home(
        { HomeScreen() },
        R.string.home_title,
        R.string.home_label,
        R.drawable.ic_home,
        R.drawable.ic_home_outlined,
    ),
    Alarm(
        { AlarmScreen() },
        R.string.alarm_title,
        R.string.alarm_label,
        R.drawable.ic_alarm,
        R.drawable.ic_alarm_outline,
    ),
    Light(
        { LightScreen() },
        R.string.light_title,
        R.string.light_label,
        R.drawable.ic_light_filled,
        R.drawable.ic_light_outlined,
    ),
    Player(
        { PlayerScreen() },
        R.string.player_title,
        R.string.player_label,
        R.drawable.ic_player,
        R.drawable.ic_player_outlined,
    ),
    Sounds(
        { SoundsScreen() },
        R.string.sounds_title,
        R.string.sounds_label,
        R.drawable.ic_sounds,
        R.drawable.ic_sounds_outlined,
    ),
    ;

    companion object {
        @Composable
        fun BottomBar(navController: NavHostController, onClick: () -> Unit) {
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute by remember(currentBackStackEntry) {
                derivedStateOf { currentBackStackEntry?.destination?.route }
            }
            NavigationBar {
                values().forEach {
                    NavigationBarItem(
                        selected = currentRoute == it.name,
                        onClick = {
                            navController.navigate(it.name) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            onClick()
                        },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    when (currentRoute) {
                                        it.name -> it.iconFilled
                                        else -> it.iconOutlined
                                    }
                                ),
                                contentDescription = stringResource(it.title),
                            )
                        },
                        label = { Text(stringResource(it.label)) },
                    )
                }
            }
        }
    }
}