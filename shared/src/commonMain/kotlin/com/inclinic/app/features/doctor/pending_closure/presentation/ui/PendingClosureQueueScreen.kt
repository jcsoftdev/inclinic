package com.inclinic.app.features.doctor.pending_closure.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun PendingClosureQueueScreen(
    component: PendingClosureQueueComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // ── AppHeader ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack)
            Text(
                text = "Citas por cerrar",
                style = typography.titleLarge,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Lucide.Clock,
                contentDescription = null,
                tint = colors.amber,
                modifier = Modifier.size(20.dp),
            )
        }

        // ── Content ────────────────────────────────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
        ) {
            ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

            if (state.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimens.spacingXl),
                ) {
                    CircularProgressIndicator(color = colors.navy)
                }
            } else if (state.items.isEmpty()) {
                PendingClosureEmptyCard()
            } else {
                state.items.forEach { item ->
                    PendingClosureCard(
                        item = item,
                        onClick = { component.onAppointmentTapped(item.id) },
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun PendingClosureEmptyCard() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacingXl),
    ) {
        Text(
            text = "No tienes citas pendientes de cerrar.",
            style = AppTheme.typography.subtitle,
            color = colors.muted,
        )
    }
}

// ── Queue card ────────────────────────────────────────────────────────────────

@Composable
private fun PendingClosureCard(
    item: PendingClosureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        // Row 1: amount
        Text(
            text = item.priceLabel,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
        )

        // Row 2: patient avatar + name · date · visit type
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.sand),
            ) {
                Text(
                    text = item.patientInitials,
                    color = colors.navy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.patientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                Text(
                    text = "${shortDateTime(item.startTime)} · ${visitTypeLabel(item.visitType)}",
                    style = typography.caption,
                    color = colors.muted,
                )
            }
        }

        // Row 3: specialty
        Text(
            text = item.specialtyName,
            fontSize = 12.sp,
            color = colors.muted,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun visitTypeLabel(visitType: String): String = when (visitType.uppercase()) {
    "VIRTUAL" -> "Virtual"
    "HOME"    -> "A domicilio"
    else      -> "Clínica"
}

/**
 * Extracts a short "Day/Mon HH:mm" label from an ISO-8601 string such as
 * "2026-06-29T10:00:00.000Z". Pure string parsing, no date library needed —
 * verbatim copy of NoShowQueueScreen.kt's shortDateTime, kept private per-file
 * (the No-Shows feature doesn't expose a shared date-formatting util to reuse).
 */
private fun shortDateTime(iso: String): String = try {
    val datePart = iso.substring(0, 10)
    val timePart = iso.substring(11, 16)
    val (_, month, day) = datePart.split("-")
    val monthName = when (month.toInt()) {
        1  -> "Ene"; 2  -> "Feb"; 3  -> "Mar"; 4  -> "Abr"
        5  -> "May"; 6  -> "Jun"; 7  -> "Jul"; 8  -> "Ago"
        9  -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dic"
    }
    "$day $monthName · $timePart"
} catch (_: Exception) { iso.take(16) }
