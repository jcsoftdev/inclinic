package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.inclinic.app.ui.theme.AppTheme

/**
 * Reusable confirmation dialog atom for destructive / irreversible actions
 * (cancel, reject, discard, logout, …).
 *
 * Replaces ad-hoc `AlertDialog` usages going forward — new confirmations should
 * use this instead of building a one-off `AlertDialog`. Existing ad-hoc dialogs
 * are not retrofitted.
 *
 * @param title         Dialog headline — short question, e.g. "¿Cerrar sesión?".
 * @param message       Supporting copy explaining the consequence of confirming.
 * @param onConfirm     Invoked when the user taps [confirmText].
 * @param onDismiss     Invoked when the user taps [dismissText] or dismisses the dialog
 *                      (scrim tap / back gesture).
 * @param confirmText   Confirm button label — "Confirmar" by default.
 * @param dismissText   Dismiss button label — "Cancelar" by default.
 * @param isDestructive When true (default), the confirm button and message use the
 *                      error color tokens to signal an irreversible/destructive action.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    isDestructive: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Text(text = title, color = colors.text, fontWeight = FontWeight.Bold)
        },
        text = {
            if (isDestructive) {
                Text(
                    text = message,
                    color = colors.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.radius))
                        .background(colors.errorBg)
                        .padding(dimens.spacing12),
                )
            } else {
                Text(text = message, color = colors.muted)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) colors.error else colors.navy,
                    contentColor = Color.White,
                ),
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = colors.muted)
            }
        },
    )
}
