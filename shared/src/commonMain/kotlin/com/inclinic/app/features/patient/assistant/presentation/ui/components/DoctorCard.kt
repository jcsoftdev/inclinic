package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.inclinic.app.features.patient.assistant.core.model.tool_results.DoctorResult
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.theme.AppTheme

/**
 * Card displaying a single doctor result from the `searchDoctors` tool.
 *
 * Tapping "Reservar" calls [onReserve] — which in the parent pre-fills the input
 * with "Quiero agendar con {doctorName}" via [AssistantChatComponent.onDoctorCardReserve].
 *
 * NOTE: No [String.format] — price is rendered via integer arithmetic to stay KMP-safe.
 */
@Composable
fun DoctorCard(
    doctor: DoctorResult,
    onReserve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    // KMP-safe price display: avoid String.format / Float.format (Java-only)
    val priceDisplay = "S/ ${((doctor.consultationPrice * 100).toLong() / 100.0)}"

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(dimens.spacingMd)) {
            Text(
                text = doctor.name,
                style = typography.titleLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)),
                color = colors.text,
            )
            if (doctor.bio.isNotBlank()) {
                Spacer(Modifier.height(dimens.spacingXs))
                Text(
                    text = doctor.bio,
                    style = typography.body,
                    color = colors.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(dimens.spacingXs))
            Text(
                text = priceDisplay,
                style = typography.body,
                color = colors.text,
            )
            doctor.ratingAvg?.let { avg ->
                Spacer(Modifier.height(dimens.spacingXs))
                Text(
                    text = "★ $avg (${doctor.ratingCount})",
                    style = typography.body,
                    color = colors.amber,
                )
            }
            Spacer(Modifier.height(dimens.spacingSm))
            AppButton(
                text = "Reservar",
                onClick = onReserve,
                size = AppButtonSize.Sm,
            )
        }
    }
}
