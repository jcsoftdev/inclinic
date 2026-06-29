package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Non-blocking informational banner shown at the top of the chat screen.
 * Visible by default; dismissed per-session via [onDismiss].
 *
 * When [visible] is false this composable emits nothing.
 */
@Composable
fun DisclaimerBanner(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.lav50)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingXs),
    ) {
        Text(
            text = "ℹ Esto no es un diagnóstico médico. Para emergencias, llama al 116.",
            style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
            color = colors.muted,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(dimens.spacingSm))
        Text(
            text = "✕",
            fontSize = 14.sp,
            color = colors.muted,
            modifier = Modifier.clickable(onClick = onDismiss),
        )
    }
}
