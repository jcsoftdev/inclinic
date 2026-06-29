package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult
import com.inclinic.app.ui.theme.AppTheme

/**
 * Red-background card shown when [BookingResult.Failed] is received from the `bookAppointment` tool.
 * Renders the backend [BookingResult.Failed.message] (already in Spanish) and a hint.
 */
@Composable
fun BookingFailCard(
    result: BookingResult.Failed,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shapes = AppTheme.shapes
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.redBg, shape = shapes.medium)
            .padding(dimens.spacingMd),
    ) {
        Text(
            text = result.message,
            style = typography.body,
            color = colors.red,
        )
        Spacer(Modifier.height(dimens.spacingXs))
        Text(
            text = "Intenta con otro horario.",
            style = typography.body,
            color = colors.muted,
        )
    }
}
