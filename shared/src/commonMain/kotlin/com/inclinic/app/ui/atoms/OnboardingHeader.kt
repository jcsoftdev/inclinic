package com.inclinic.app.ui.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Doctor onboarding header bar — matches the ClinicAI `design.pen` `header` frame
 * (id `Urlrs`): a back button followed by the constant "Crear cuenta" title.
 *
 * The title uses [AppTypography.displayNano] (Funnel Sans 22sp bold), the exact
 * token reserved for the onboarding header in the design system.
 *
 * @param onBack invoked when the back button is tapped.
 * @param title  header title; defaults to the design's "Crear cuenta".
 */
@Composable
fun OnboardingHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Crear cuenta",
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppBackButton(onClick = onBack)
        Text(
            text = title,
            style = AppTheme.typography.displayNano,
            color = colors.text,
        )
    }
}
