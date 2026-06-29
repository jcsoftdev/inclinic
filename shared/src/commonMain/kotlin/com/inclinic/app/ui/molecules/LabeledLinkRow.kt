package com.inclinic.app.ui.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.atoms.AppLink
import com.inclinic.app.ui.theme.AppTheme

/**
 * Centered row of muted prefix text followed by an emphasized action link.
 *
 * Used for "¿Eres paciente? Regístrate aquí" style call-to-actions.
 */
@Composable
fun LabeledLinkRow(
    prefix: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = prefix,
            style = AppTheme.typography.link,
            color = AppTheme.colors.muted,
        )
        Spacer(Modifier.width(4.dp))
        AppLink(text = action, onClick = onClick, emphasized = true)
    }
}
