package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.admin.infrastructure.remote.AdminTopDoctor
import com.inclinic.app.features.admin.presentation.component.AdminFinanceComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinanceScreen(component: AdminFinanceComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.sand)
            .padding(innerPadding),
    ) {
        if (state.isLoading && state.balanceTotal == "S/ 0") {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // ── Header ────────────────────────────────────────────────────
                FinanceHeader(
                    onBack = component::onBack,
                    onExport = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Exportación disponible próximamente")
                        }
                    },
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.error?.let { err ->
                        Text(err, style = AppTheme.typography.subtitle, color = colors.red)
                    }

                    // ── Hero card: Balance disponible ─────────────────────────
                    FinanceHeroCard(
                        balanceTotal = state.balanceTotal,
                        released = state.released,
                        held = state.held,
                    )

                    // ── 2 metric tiles ────────────────────────────────────────
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FinanceMetricTile(
                            label = "Ingresos",
                            value = state.thisMonthRevenue,
                            subText = "este mes",
                            icon = null,
                            modifier = Modifier.weight(1f),
                        )
                        FinanceMetricTile(
                            label = "Retenido",
                            value = state.heldAmount,
                            subText = "${state.heldCount} casos",
                            icon = Lucide.Lock,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // ── Section label ─────────────────────────────────────────
                    Text(
                        "MOVIMIENTOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = colors.muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    // ── Top doctors list ──────────────────────────────────────
                    if (state.topDoctors.isEmpty()) {
                        EmptyState(
                            title = "Sin movimientos",
                            subtitle = "No hay transacciones registradas en los últimos 30 días.",
                            icon = Lucide.HandCoins,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.topDoctors.forEach { doctor ->
                                DoctorMovimientoRow(doctor = doctor)
                            }
                        }
                    }
                }
            }
        }
    }
    } // Scaffold
}

// ── Header ─────────────────────────────────────────────────────────────────────

@Composable
private fun FinanceHeader(onBack: () -> Unit, onExport: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppBackButton(onClick = onBack)
        Text(
            "Finanzas",
            style = AppTheme.typography.displayXSmall,
            fontSize = 22.sp,
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AppTheme.dimens.radius))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))
                .clickable { onExport() },
        ) {
            Icon(
                imageVector = Lucide.Download,
                contentDescription = "Exportar",
                tint = colors.navy,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Hero card ──────────────────────────────────────────────────────────────────

@Composable
private fun FinanceHeroCard(
    balanceTotal: String,
    released: String,
    held: String,
) {
    val colors = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.navy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "BALANCE DISPONIBLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Color.White.copy(alpha = 0.75f),
        )
        Text(
            balanceTotal,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            "Pagos liberados $released · Retenidos $held",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.75f),
        )
    }
}

// ── Metric tile ────────────────────────────────────────────────────────────────

@Composable
private fun FinanceMetricTile(
    label: String,
    value: String,
    subText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.muted)
        }
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Text(subText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.muted)
    }
}

// ── Doctor movimiento row ──────────────────────────────────────────────────────

@Composable
private fun DoctorMovimientoRow(doctor: AdminTopDoctor) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                doctor.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                "${doctor.appointments} citas · S/ ${formatFinanceMoney(doctor.totalRevenue)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.muted,
            )
        }
        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.light,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Money formatter (full, no compaction — consistent with finance context) ────

private fun formatFinanceMoney(value: Double): String {
    val rounded = value.toLong()
    return buildString {
        val s = rounded.toString()
        var count = 0
        for (i in s.indices.reversed()) {
            if (count > 0 && count % 3 == 0) insert(0, ',')
            insert(0, s[i])
            count++
        }
    }
}
