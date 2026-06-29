package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Paperclip
import com.composables.icons.lucide.Send
import com.inclinic.app.ui.theme.AppTheme

/**
 * Chat input bar with a rounded text field, a paperclip attachment button,
 * and a circular send button.
 *
 * Triangulation skipped: pure Composable, no extractable logic.
 *
 * @param value         Current text field value.
 * @param onValueChange Called when the user types.
 * @param onSend        Called when the user taps the send button.
 * @param onAttach      Called when the user taps the paperclip button.
 * @param modifier      Modifier forwarded to the root row.
 */
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
    ) {
        IconButton(onClick = onAttach, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector        = Lucide.Paperclip,
                contentDescription = "Adjuntar archivo",
                tint               = colors.muted,
                modifier           = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(dimens.spacingXs))

        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text("Escribe un mensaje…", style = AppTheme.typography.body, color = colors.light) },
            singleLine    = false,
            maxLines      = 4,
            shape         = AppTheme.shapes.pill,
            colors        = TextFieldDefaults.colors(
                focusedContainerColor   = colors.lav50,
                unfocusedContainerColor = colors.lav50,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor        = colors.text,
                unfocusedTextColor      = colors.text,
            ),
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.width(dimens.spacingXs))

        IconButton(
            onClick  = onSend,
            modifier = Modifier
                .size(40.dp)
                .background(colors.navy, CircleShape),
        ) {
            Icon(
                imageVector        = Lucide.Send,
                contentDescription = "Enviar mensaje",
                tint               = Color.White,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}
