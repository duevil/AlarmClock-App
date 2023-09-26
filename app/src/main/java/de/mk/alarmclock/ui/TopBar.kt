package de.mk.alarmclock.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mk.alarmclock.R
import de.mk.alarmclock.ui.base.Icons

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar(onRefresh: () -> Unit, onSettings: () -> Unit, showRefreshBadge: Boolean) {
    TopAppBar(
        title = {
            Text(
                color = MaterialTheme.colorScheme.tertiary,
                text = stringResource(R.string.top_app_bar_title),
                fontSize = LocalTextStyle.current.fontSize * .8f,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            Button(
                onRefresh,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = LocalContentColor.current,
                ),
            ) { BadgedBox({ if (showRefreshBadge) Badge() }) { Icons.Refresh() } }
            IconButton(onSettings) { Icons.Settings() }
        },
    )
}

