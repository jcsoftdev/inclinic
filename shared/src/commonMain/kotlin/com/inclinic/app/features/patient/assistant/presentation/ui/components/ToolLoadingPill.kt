package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.features.patient.assistant.core.model.ToolName
import com.inclinic.app.ui.theme.AppTheme

/**
 * Small horizontal pill shown while a tool call is in-flight.
 * Renders a [CircularProgressIndicator] + Spanish label per [ToolName].
 */
@Composable
fun ToolLoadingPill(
    toolName: ToolName,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shapes = AppTheme.shapes

    val label = when (toolName) {
        ToolName.LIST_SPECIALTIES  -> "Cargando especialidades..."
        ToolName.SEARCH_DOCTORS    -> "Buscando doctores disponibles..."
        ToolName.GET_AVAILABILITY  -> "Revisando disponibilidad..."
        ToolName.BOOK_APPOINTMENT  -> "Agendando tu cita..."
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = colors.lav50, shape = shapes.pill)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingXs),
    ) {
        CircularProgressIndicator(
            color = colors.muted,
            strokeWidth = 1.5.dp,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(dimens.spacingSm))
        Text(
            text = label,
            color = colors.muted,
            fontSize = 12.sp,
        )
    }
}
