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
import androidx.compose.ui.unit.sp
import com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult
import com.inclinic.app.ui.theme.AppTheme

/**
 * Green-tint card shown when [BookingResult.Ok] is received from the `bookAppointment` tool.
 *
 * Displays the appointment ID and a non-clickable payment placeholder (v1).
 */
@Composable
fun BookingSuccessCard(
    result: BookingResult.Ok,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shapes = AppTheme.shapes
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.successBg,
                shape = shapes.medium,
            )
            .padding(dimens.spacingMd),
    ) {
        Text(
            text = "✓ Cita reservada",
            style = typography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = colors.green,
        )
        Spacer(Modifier.height(dimens.spacingXs))
        Text(
            text = "ID: ${result.appointmentId}",
            style = typography.body,
            color = colors.text,
        )
        Spacer(Modifier.height(dimens.spacingXs))
        Text(
            text = "Pago pendiente: ${result.paymentRedirectPath}",
            style = typography.body,
            color = colors.muted,
        )
        Text(
            text = "(próximamente)",
            fontSize = 11.sp,
            color = colors.muted,
        )
    }
}
