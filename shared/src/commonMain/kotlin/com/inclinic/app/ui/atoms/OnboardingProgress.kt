package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Onboarding step indicator matching the Pencil spec:
 * navy step caption + a row of segments (filled navy up to [current], rest border).
 */
@Composable
fun OnboardingProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(modifier = modifier) {
        Text(
            text  = "Paso $current de $total",
            style = AppTheme.typography.caption,
            color = colors.navy,
        )
        Spacer(Modifier.height(AppTheme.dimens.spacingSm))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(total) { index ->
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            color = if (index < current) colors.navy else colors.border,
                            shape = RoundedCornerShape(3.dp),
                        ),
                )
            }
        }
    }
}
