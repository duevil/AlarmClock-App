package de.mk.alarmclock.ui.base.items

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextItem(
    modifier: Modifier = Modifier,
    @StringRes headlineTextR: Int? = null,
    headlineText: String? = headlineTextR?.let { stringResource(it) },
    overlineText: String? = null,
    supportingText: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit) = {},
    onLongClick: (() -> Unit) = {},
    onDoubleClick: (() -> Unit) = {},
) {
    ListItem(
        headlineContent = {
            headlineText?.let { Text(text = it, style = MaterialTheme.typography.headlineSmall) }
        },
        overlineContent = overlineText?.let { { Text(it) } },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
            )
            .padding(0.dp),
    )
}