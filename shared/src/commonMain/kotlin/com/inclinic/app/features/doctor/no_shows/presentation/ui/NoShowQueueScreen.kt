package com.inclinic.app.features.doctor.no_shows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Flag
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem
import com.inclinic.app.features.doctor.no_shows.core.model.PaymentHoldStatus
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueComponent
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueState
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowTab
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun NoShowQueueScreen(
    component: NoShowQueueComponent,
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
                text = "No-shows",
                style = typography.titleLarge,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Lucide.Flag,
                contentDescription = null,
                tint = colors.amber,
                modifier = Modifier.size(20.dp),
            )
        }

        // ── TabBar ─────────────────────────────────────────────────────────────
        NoShowTabBar(
            selectedTab = state.selectedTab,
            pendingCount = state.pending.size,
            resolvedCount = state.resolved.size,
            onTabSelected = component::onTabSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
        )

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
            } else {
                val items = if (state.selectedTab == NoShowTab.Pending) state.pending else state.resolved
                if (items.isEmpty()) {
                    NoShowEmptyCard(tab = state.selectedTab)
                } else {
                    items.forEach { item ->
                        NoShowCard(item = item)
                    }
                }
            }
        }
    }
}

// ── TabBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NoShowTabBar(
    selectedTab: NoShowTab,
    pendingCount: Int,
    resolvedCount: Int,
    onTabSelected: (NoShowTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radius)),
    ) {
        TabItem(
            label = "Pendientes",
            count = pendingCount,
            selected = selectedTab == NoShowTab.Pending,
            onClick = { onTabSelected(NoShowTab.Pending) },
            modifier = Modifier.weight(1f),
        )
        TabItem(
            label = "Resueltos",
            count = resolvedCount,
            selected = selectedTab == NoShowTab.Resolved,
            onClick = { onTabSelected(NoShowTab.Resolved) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius))
            .background(if (selected) colors.navy else colors.surface)
            .then(
                if (selected) Modifier else androidx.compose.ui.Modifier.border(
                    0.dp,
                    colors.border,
                    RoundedCornerShape(dimens.radius),
                )
            )
            .padding(vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) androidx.compose.ui.graphics.Color.White else colors.muted,
            )
            if (count > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f)
                            else colors.sand
                        ),
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) androidx.compose.ui.graphics.Color.White else colors.navy,
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun NoShowEmptyCard(tab: NoShowTab) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val message = if (tab == NoShowTab.Pending) {
        "No tienes no-shows pendientes de resolución."
    } else {
        "No hay no-shows resueltos todavía."
    }
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
            text = message,
            style = AppTheme.typography.subtitle,
            color = colors.muted,
        )
    }
}

// ── Queue card ────────────────────────────────────────────────────────────────

@Composable
private fun NoShowCard(
    item: NoShowItem,
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
            .padding(16.dp),
    ) {
        // Row 1: amount + optional resolved chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = item.priceLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
            )
            holdStatusChip(item.paymentHoldStatus)?.let { (label, kind) ->
                ChipStatus(label = label, kind = kind)
            }
        }

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

        // Row 4: reason (if present)
        item.reason?.let { reason ->
            Text(
                text = "Motivo: \"$reason\"",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = colors.muted,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun holdStatusChip(status: PaymentHoldStatus): Pair<String, ChipStatusKind>? = when (status) {
    PaymentHoldStatus.HELD     -> null // pending tab — no resolved chip
    PaymentHoldStatus.RELEASED -> "LIBERADO"  to ChipStatusKind.Success
    PaymentHoldStatus.REFUNDED -> "REEMBOLSADO" to ChipStatusKind.Info
    PaymentHoldStatus.UNKNOWN  -> null
}

private fun visitTypeLabel(visitType: String): String = when (visitType.uppercase()) {
    "VIRTUAL" -> "Virtual"
    "HOME"    -> "A domicilio"
    else      -> "Clínica"
}

/**
 * Extracts a short "Day/Mon HH:mm" label from an ISO-8601 string such as
 * "2026-06-29T10:00:00.000Z".  Pure string parsing, no date library needed.
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
