package de.mk.alarmclock.ui.base.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp

@Composable
fun SmallTextItem(
    text: String,
    textStyle: TextStyle
    = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface((onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier).fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.let {
                it()
                Spacer(Modifier.width(16.dp))
            }
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = text,
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            trailingContent?.let { it() }
        }
    }
}