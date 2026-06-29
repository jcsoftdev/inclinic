package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

// ── Pure helper (unit-testable) ───────────────────────────────────────────────

/**
 * Returns the horizontal alignment for a chat bubble based on ownership.
 * [isMine] = true → [Alignment.End] (right side).
 */
fun chatBubbleAlignment(isMine: Boolean): Alignment.Horizontal =
    if (isMine) Alignment.End else Alignment.Start

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * Single chat message bubble.
 *
 * @param text      Message body.
 * @param isMine    True for outgoing messages (right-aligned, blue bubble).
 * @param timestamp Formatted time string shown below the message.
 * @param modifier  Modifier forwarded to the root column.
 */
@Composable
fun ChatBubble(
    text: String,
    isMine: Boolean,
    timestamp: String,
    modifier: Modifier = Modifier,
) {
    val colors    = AppTheme.colors
    val alignment = chatBubbleAlignment(isMine)
    val bubbleBg  = if (isMine) colors.navy else colors.lav50
    val textColor = if (isMine) Color.White else colors.text

    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bubbleBg, if (isMine) AppTheme.shapes.large else AppTheme.shapes.large)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text  = text,
                style = AppTheme.typography.body,
                color = textColor,
            )
        }
        Text(
            text  = timestamp,
            style = AppTheme.typography.caption,
            color = colors.light,
        )
    }
}
