package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Red-background error banner atom.
 *
 * Renders nothing when [message] is null — callers can pass null freely
 * without wrapping in an if-block.
 *
 * @param message Error string to display, or null to hide the banner.
 * @param modifier Modifier forwarded to the banner root Box.
 */
@Composable
fun ErrorBanner(
    message: String?,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (message == null) return

    val colors = AppTheme.colors
    val shape  = RoundedCornerShape(AppTheme.dimens.radius)

    Box(
        modifier = modifier
            .background(color = colors.redBg, shape = shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text  = message,
            color = colors.red,
            fontSize = 13.sp,
        )
    }
}
