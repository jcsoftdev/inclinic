package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inclinic.app.features.patient.assistant.core.model.AssistantMessage
import com.inclinic.app.ui.theme.AppTheme

/**
 * Renders a single chat bubble for [AssistantMessage.User] or [AssistantMessage.Assistant].
 *
 * - User   → right-aligned, navy background, white text
 * - Assistant → left-aligned, lavLight background, text color
 *
 * [AssistantMessage.ToolResultCard] is rendered separately via tool-specific composables
 * and MUST NOT be passed to this function.
 */
@Composable
fun MessageBubble(
    message: AssistantMessage,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shapes = AppTheme.shapes
    val typography = AppTheme.typography

    val isUser = message is AssistantMessage.User
    val text = when (message) {
        is AssistantMessage.User      -> message.text
        is AssistantMessage.Assistant -> message.text
        is AssistantMessage.ToolResultCard -> return  // no-op: rendered by tool components
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Text(
            text = text,
            style = typography.body,
            color = if (isUser) Color.White else colors.text,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isUser) colors.navy else colors.lavLight,
                    shape = shapes.medium,
                )
                .padding(dimens.spacingMd),
        )
    }
}
