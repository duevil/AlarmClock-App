package de.mk.alarmclock.ui.base.items

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StandardItem(
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
) {
    ListItem(
        headlineContent = { headlineContent() },
        supportingContent = supportingContent?.let { { it() } },
        leadingContent = leadingContent?.let { { it() } },
        trailingContent = trailingContent?.let { { it() } },
        modifier = onClick?.let { modifier.clickable(onClick = it) } ?: modifier,
    )
}