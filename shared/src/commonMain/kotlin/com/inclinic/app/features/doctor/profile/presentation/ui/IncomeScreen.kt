package com.inclinic.app.features.doctor.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Percent
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.Wallet
import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

/**
 * Ingresos screen (s84JQ — Doctor Mis Ingresos).
 *
 * Layout (Pencil):
 *  AppHeader "Mis Ingresos"
 *  IncomeHeroCard (indigo bg, monthly total + breakdown)
 *  KPI row (2 cards: consultas + paquetes)
 *  chart card (Ingresos por semana — bars placeholder, no backend time-series)
 *  withdrawal card (green bg)
 *  commission info banner
 *
 * Note: the backend (GET /api/doctors/me/metrics) provides only monthly-aggregate
 * income data — no time-series bars. The chart shows an "Información no disponible"
 * placeholder rather than fabricated numbers.
 */
@Composable
fun IncomeScreen(
    component: IncomeComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val dimens = AppTheme.dimens
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize().background(colors.sand)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // ── Header ─────────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                ) {
                    AppBackButton(onClick = component::onBack)
                    Spacer(Modifier.width(dimens.spacing12))
                    Text(
                        text = "Mis Ingresos",
                        style = typography.titleLarge,
                        color = colors.text,
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12 + 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
                ) {
                    val summary = state.summary
                    if (summary != null) {
                        IncomeHeroCard(summary)
                        KpiRow(summary)
                        ChartCard()
                        WithdrawalCard(summary)
                        CommissionBanner()
                    } else if (state.error != null) {
                        Text(
                            text = state.error ?: "Error desconocido",
                            style = typography.body,
                            color = colors.red,
                            modifier = Modifier.fillMaxWidth().padding(vertical = dimens.spacingMd),
                        )
                        AppButton(
                            text = "Reintentar",
                            onClick = component::onRetry,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

private fun formatSoles(cents: Long): String = "S/ ${cents / 100}"
private fun formatSolesDirect(amount: Double): String = "S/ ${amount.toLong()}"

@Composable
private fun IncomeHeroCard(summary: IncomeSummary) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm + 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.navy)
            .padding(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "INGRESOS DEL MES",
                    color = colors.lavLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = formatSoles(summary.totalCents),
                    style = typography.displayLarge,
                    color = Color.White,
                )
            }
            if (summary.growthPct != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = dimens.spacing12 - 2.dp, vertical = 4.dp),
                ) {
                    Icon(Lucide.TrendingUp, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(12.dp))
                    Text(
                        text = "${if (summary.growthPct >= 0) "+" else ""}${summary.growthPct.toInt()}%",
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12 + 2.dp),
            modifier = Modifier.fillMaxWidth().padding(top = dimens.spacingXs + 2.dp),
        ) {
            HeroBreakdown("Neto", formatSoles(summary.netCents), Modifier.weight(1f))
            HeroBreakdown("Comisión", formatSoles(summary.commissionCents), Modifier.weight(1f))
            HeroBreakdown("Consultas", "${summary.sessions}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroBreakdown(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = modifier) {
        Text(text = label, color = colors.lavLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KpiRow(summary: IncomeSummary) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12 - 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Consultas KPI
        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXs + 2.dp),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(dimens.radiusMd + 2.dp))
                .background(colors.surface)
                .padding(dimens.spacingMd - 2.dp),
        ) {
            Text("Sesiones", style = typography.label, color = colors.muted)
            Text(
                text = "${summary.sessions}",
                style = typography.displayNano,
                color = colors.text,
            )
        }
        // Net KPI
        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXs + 2.dp),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(dimens.radiusMd + 2.dp))
                .background(colors.surface)
                .padding(dimens.spacingMd - 2.dp),
        ) {
            Text("Ingreso neto", style = typography.label, color = colors.muted)
            Text(
                text = formatSoles(summary.netCents),
                style = typography.displayNano,
                color = colors.navy,
            )
        }
    }
}

@Composable
private fun ChartCard() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm + 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd - 2.dp),
    ) {
        Text(
            text = "Ingresos por semana",
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        // Placeholder — no time-series data available from backend
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(dimens.radius))
                .background(colors.sand),
        ) {
            Text(
                text = "Datos semanales próximamente",
                style = typography.caption,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun WithdrawalCard(summary: IncomeSummary) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val greenBg = Color(0xFF14352A)
    val green = Color(0xFF34D399)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12 - 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(greenBg)
            .padding(dimens.spacingMd - 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(green),
        ) {
            Icon(Lucide.Wallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(text = "Disponible para retiro", color = green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = formatSoles(summary.availableCents), color = green, style = typography.titleLarge)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(dimens.radius))
                .background(green)
                .padding(horizontal = dimens.spacingMd - 2.dp, vertical = dimens.spacingSm),
        ) {
            Text(text = "Retirar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CommissionBanner() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius))
            .background(colors.surface)
            .padding(dimens.spacingMd - 2.dp),
    ) {
        Icon(Lucide.Percent, contentDescription = null, tint = colors.navy, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text("Comisión plataforma", style = typography.body, color = colors.navy, fontWeight = FontWeight.Bold)
            Text(
                text = "Plan FREE 15% por consulta · PREMIUM 5% + S/ 99/mes",
                style = typography.caption,
                color = colors.navy,
            )
        }
    }
}
