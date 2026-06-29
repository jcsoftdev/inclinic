package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Square
import com.inclinic.app.ui.theme.AppTheme

/**
 * Chat input bar at the bottom of [AssistantChatScreen].
 *
 * Visual states:
 * - **Idle / cooldown**: `+` circle button | pill input field (mic icon) | circular send button.
 *   When [cooldownSeconds] is not null, the placeholder shows "Espera {N}s" instead.
 * - **Streaming**: disabled pill input with "Generando respuesta…" placeholder | red stop button.
 *
 * Behaviour:
 * - [enabled] = !isStreaming && retryAfterSeconds == null
 * - [isStreaming] drives the stop button appearance; caller wires stop via [onSend] (no-op while
 *   streaming from the component side — component ignores extra sends during stream).
 *   A future `onStop` callback can be threaded in without breaking this API.
 */
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    cooldownSeconds: Int?,
    isStreaming: Boolean = false,
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    val pillShape = CircleShape
    // Input pill bg: design uses sand (#0A0B14) for the pill background
    val inputBg = colors.sand
    val barBg = colors.surface   // #12141F — design's inputBar frame

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(barBg)
            .drawBehind {
                drawLine(
                    color = colors.border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
    ) {
        // ── + button (hidden while streaming) ────────────────────────────────
        if (!isStreaming) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(pillShape)
                    .background(inputBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = "Adjuntar",
                    tint = colors.text,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // ── Input pill ───────────────────────────────────────────────────────
        val placeholder = when {
            isStreaming    -> "Generando respuesta…"
            cooldownSeconds != null -> "Espera ${cooldownSeconds}s…"
            else           -> "Escribe un mensaje…"
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 42.dp)
                .clip(CircleShape)
                .background(inputBg)
                .border(width = 1.dp, color = colors.border, shape = CircleShape)
                .padding(horizontal = 16.dp, vertical = 11.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled && !isStreaming,
                        maxLines = 3,
                        textStyle = typography.body.copy(color = colors.text),
                        cursorBrush = SolidColor(colors.navy),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default,
                        ),
                    )
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = typography.body,
                            color = colors.light,
                        )
                    }
                }
                // Mic icon (design) — only when not streaming
                if (!isStreaming) {
                    Icon(
                        imageVector = Lucide.Mic,
                        contentDescription = null,
                        tint = colors.muted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // ── Send / Stop button ────────────────────────────────────────────────
        if (isStreaming) {
            // Red stop button (square icon inside error-bg circle)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.errorBg)
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Lucide.Square,
                    contentDescription = "Detener",
                    tint = colors.red,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            // Navy send button (paper-plane icon)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (enabled) colors.navy else colors.light)
                    .clickable(enabled = enabled, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Lucide.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
