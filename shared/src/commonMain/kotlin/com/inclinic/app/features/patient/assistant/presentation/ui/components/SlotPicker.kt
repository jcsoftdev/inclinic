package com.inclinic.app.features.patient.assistant.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.inclinic.app.features.patient.assistant.core.model.tool_results.AvailabilitySlot
import com.inclinic.app.ui.theme.AppTheme

/**
 * Grid of available time slots from the `getDoctorAvailability` tool result.
 *
 * - Available slots → tappable chip, calls [onSlotSelected] with the slot time string.
 * - Taken slots     → alpha 0.4, non-tappable.
 * - Empty list      → "Sin horarios disponibles."
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlotPicker(
    date: String,
    slots: List<AvailabilitySlot>,
    onSlotSelected: (time: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val shapes = AppTheme.shapes

    Column(modifier = modifier) {
        Text(
            text = "Horarios para $date",
            style = typography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            color = colors.text,
        )
        Spacer(Modifier.height(dimens.spacingSm))

        if (slots.isEmpty()) {
            Text(
                text = "Sin horarios disponibles.",
                style = typography.body,
                color = colors.muted,
            )
            return
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            maxItemsInEachRow = 4,
        ) {
            slots.forEach { slot ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .alpha(if (slot.available) 1f else 0.4f)
                        .background(
                            color = if (slot.available) colors.lav50 else colors.border,
                            shape = shapes.small,
                        )
                        .then(
                            if (slot.available) {
                                Modifier.clickable { onSlotSelected(slot.time) }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = dimens.spacingSm, vertical = dimens.spacingXs),
                ) {
                    Text(
                        text = slot.time,
                        style = typography.body,
                        color = if (slot.available) colors.navy else colors.muted,
                    )
                }
            }
        }
    }
}
