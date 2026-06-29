package com.inclinic.app.features.doctor.packages.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.packages.core.model.PackageSession
import com.inclinic.app.features.doctor.packages.core.model.PackageSessionStatus
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun PackageDetailScreen(component: PackageDetailComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack)
            Text(
                text = "Detalle Paquete",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        Box(Modifier.fillMaxSize()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }

                state.error != null && state.pkg == null -> ErrorContent(
                    message = state.error!!,
                    onRetry = component::onRetry,
                )

                state.pkg != null -> LoadedContent(
                    pkg = state.pkg!!,
                    isCancelling = state.isCancelling,
                    error = state.error,
                    onCancel = component::onCancel,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier.padding(horizontal = dimens.spacingMd),
        ) {
            Text(message, color = colors.red, style = typography.body)
            AppButton(
                text = "Reintentar",
                onClick = onRetry,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Md,
            )
        }
    }
}

@Composable
private fun LoadedContent(
    pkg: TherapyPackage,
    isCancelling: Boolean,
    error: String?,
    onCancel: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(dimens.spacingMd),
        ) {
            // ── Hero ──────────────────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.navy)
                    .padding(dimens.spacingMd),
            ) {
                Text(
                    text = if (pkg.isPrepaid) "PAQUETE PREPAGADO" else "PAQUETE",
                    color = colors.lavLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = pkg.packageName,
                    style = typography.titleLarge,
                    color = Color.White,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${pkg.sessionsUsed}/${pkg.totalSessions} sesiones",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "INGRESO ESPERADO",
                            color = colors.lavLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                        )
                        Text(
                            text = formatSoles(pkg.expectedIncome),
                            style = typography.titleLarge,
                            color = Color.White,
                        )
                    }
                }
            }

            // ── Patient info ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.surface)
                    .padding(dimens.spacingMd),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(dimens.radiusPill))
                        .background(colors.blueBg),
                ) {
                    Text(
                        text = pkg.patientName.firstOrNull()?.uppercase() ?: "?",
                        color = colors.blue,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(pkg.patientName, style = typography.body, fontWeight = FontWeight.Bold, color = colors.text)
                    if (pkg.patientEmail.isNotBlank()) {
                        Text(pkg.patientEmail, style = typography.subtitle, color = colors.muted)
                    }
                }
            }

            // ── Price comparison cards ────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PriceCard(
                    label = "REGULAR",
                    value = formatSoles(pkg.regularPricePerSession),
                    caption = "por sesión",
                    accent = colors.muted,
                    bg = colors.elevated,
                    border = colors.border,
                    modifier = Modifier.weight(1f),
                )
                PriceCard(
                    label = "PAQUETE",
                    value = formatSoles(pkg.packagePricePerSession),
                    caption = "por sesión",
                    accent = colors.teal,
                    bg = colors.tealBg,
                    border = colors.teal,
                    modifier = Modifier.weight(1f),
                )
                PriceCard(
                    label = "PREPAGO",
                    value = if (pkg.isPrepaid) "−${pkg.prepaidDiscountPercent}%" else "—",
                    caption = "descuento",
                    accent = colors.purple,
                    bg = colors.purpleBg,
                    border = colors.purple,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Sessions ──────────────────────────────────────────────────────
            if (pkg.sessions.isNotEmpty()) {
                Text(
                    text = "SESIONES AGENDADAS",
                    style = typography.label,
                    color = colors.muted,
                    letterSpacing = 0.8.sp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                    pkg.sessions.forEach { SessionRow(it) }
                }
            }

            error?.let { Text(it, color = colors.red, style = typography.body) }
        }

        // ── Actions ───────────────────────────────────────────────────────────
        if (pkg.status.name == "ACTIVE" || pkg.status.name == "PENDING_PAYMENT") {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(dimens.spacingMd),
            ) {
                AppButton(
                    text = "Cancelar paquete",
                    onClick = onCancel,
                    variant = AppButtonVariant.Danger,
                    size = AppButtonSize.Lg,
                    loading = isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                )
                AppButton(
                    text = "Agendar siguiente",
                    onClick = {},
                    enabled = false,
                    size = AppButtonSize.Lg,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(dimens.spacingSm))
    }
}

@Composable
private fun PriceCard(
    label: String,
    value: String,
    caption: String,
    accent: Color,
    bg: Color,
    border: Color,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radiusMd))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(dimens.radiusMd))
            .padding(dimens.spacing12),
    ) {
        Text(label, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Text(value, color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(caption, color = colors.muted, fontSize = 11.sp)
    }
}

@Composable
private fun SessionRow(session: PackageSession) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    val upcoming = session.status == PackageSessionStatus.UPCOMING
    val icon = if (session.status == PackageSessionStatus.COMPLETED) Lucide.CircleCheck else Lucide.Calendar
    val iconTint = when (session.status) {
        PackageSessionStatus.COMPLETED -> colors.green
        PackageSessionStatus.UPCOMING -> colors.navy
        PackageSessionStatus.UNSCHEDULED -> colors.muted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusMd))
            .background(colors.elevated)
            .then(if (upcoming) Modifier.border(1.5.dp, colors.navy, RoundedCornerShape(dimens.radiusMd)) else Modifier)
            .padding(dimens.spacing12),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                style = typography.body,
                fontWeight = FontWeight.Bold,
                color = if (session.status == PackageSessionStatus.UNSCHEDULED) colors.muted else colors.text,
            )
            Text(
                text = session.subtitle,
                style = typography.subtitle,
                color = if (upcoming) colors.navy else colors.muted,
                fontWeight = if (upcoming) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}
