package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

enum class PasswordStrength { Weak, Fair, Good, Strong }

@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val filled = strength.ordinal + 1
    val activeColor = when (strength) {
        PasswordStrength.Weak   -> colors.red
        PasswordStrength.Fair   -> colors.amber
        PasswordStrength.Good,
        PasswordStrength.Strong -> colors.green
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier              = modifier.fillMaxWidth(),
    ) {
        repeat(4) { idx ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (idx < filled) activeColor else colors.border),
            )
        }
    }
}
