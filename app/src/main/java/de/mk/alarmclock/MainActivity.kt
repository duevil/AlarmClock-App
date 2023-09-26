package de.mk.alarmclock

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.mk.alarmclock.api.RestfulCall
import de.mk.alarmclock.ui.AlarmClockControllerTheme
import de.mk.alarmclock.ui.Navigation
import de.mk.alarmclock.ui.TopBar
import de.mk.alarmclock.ui.base.PaddingBox
import de.mk.alarmclock.ui.base.items.HeadlineItem
import de.mk.alarmclock.ui.screens.SettingsScreen
import de.mk.alarmclock.ui.screens.SetupScreen
import de.mk.alarmclock.ui.screens.settingsDialog
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AlarmClockControllerTheme { Main() } }
    }
}

@Composable
private fun Main(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshing = viewModel.callsRunning()
    var isPullRefreshing by rememberSaveable { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isPullRefreshing,
        onRefresh = { isPullRefreshing = true },
    )
    val mainBoxScrollState = rememberScrollState()
    var scrollToTop by remember { mutableStateOf(false) }
    val language by viewModel.getLocalListCompatState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onRequestFailed = snackbarHostState.showRefreshFailSnackbar(context)
    }
    LaunchedEffect(isPullRefreshing) {
        if (isPullRefreshing) {
            delay(100)
            viewModel.refresh()
            isPullRefreshing = false
        }
    }
    LaunchedEffect(language) { Language.appLocalListCompat = language }
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            mainBoxScrollState.animateScrollTo(0)
            scrollToTop = false
        }
    }

    if (refreshing) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            content = { CircularProgressIndicator() },
        )
    }
    viewModel.SetupScreen()
    settingsDialog(R.string.settings_title) { viewModel.SettingsScreen() }
    Scaffold(
        bottomBar = { Navigation.BottomBar(navController) { scrollToTop = true } },
        topBar = { TopBar(viewModel::refresh, settingsDialog::show, viewModel.needToRefresh) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        PaddingBox {
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullRefresh(pullRefreshState)
                    .fillMaxSize()
                    .verticalScroll(mainBoxScrollState)
            ) {
                NavHost(navController, Navigation.Home.name) {
                    Navigation.values().forEach { nav ->
                        composable(nav.name) {
                            Column {
                                HeadlineItem(nav.title)
                                nav.screen(viewModel)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isPullRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

private fun SnackbarHostState.showRefreshFailSnackbar(context: Context)
        : suspend (RestfulCall.Type, String, (() -> Unit)?) -> Unit = { type, message, retry ->
    Log.w("MainActivity", "Request failed: $message")
    showSnackbar(
        message = "${
            when (type) {
                RestfulCall.Type.FETCH_DATA -> context.getString(R.string.fetch_data_failed)
                RestfulCall.Type.UPDATE_DATA -> context.getString(R.string.update_data_failed)
                RestfulCall.Type.RUN_TASK -> context.getString(R.string.run_task_failed)
            }
        }: $message",
        actionLabel = retry?.let { context.getString(R.string.retry) },
        withDismissAction = retry == null,
        duration = SnackbarDuration.Short,
    ).takeIf { it == SnackbarResult.ActionPerformed }?.let { if (retry != null) retry() }
}
