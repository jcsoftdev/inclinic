package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.inclinic.app.features.patient.assistant.core.model.tool_results.DoctorResult
import com.inclinic.app.ui.theme.AppTheme

/**
 * Vertical list of [DoctorCard]s from a `searchDoctors` tool result.
 *
 * Empty state: shows "No se encontraron doctores."
 *
 * @param onReserveDoctor Called with [DoctorResult.name] when the patient taps "Reservar".
 */
@Composable
fun DoctorCardGrid(
    doctors: List<DoctorResult>,
    onReserveDoctor: (doctorName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = AppTheme.dimens
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    if (doctors.isEmpty()) {
        Text(
            text = "No se encontraron doctores.",
            style = typography.body,
            color = colors.muted,
            modifier = modifier.padding(vertical = dimens.spacingSm),
        )
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        horizontalAlignment = Alignment.Start,
    ) {
        doctors.forEach { doctor ->
            DoctorCard(
                doctor = doctor,
                onReserve = { onReserveDoctor(doctor.name) },
            )
        }
    }
}
