package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorStatus
import com.inclinic.app.features.admin.presentation.component.AdminConfigComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

/**
 * Configuración screen — honest settings derived from real backend data only.
 *
 * Design node: tIT2n (stub replaced with honest content).
 *
 * GENERAL section shows real 2FA enforcement/enabled status from getTwoFactorStatus.
 * AUTOMATIZACIÓN section is intentionally omitted: there is no AdminSettings backend
 * model yet, so fake toggles that persist nothing are not rendered.
 *
 * TODO: real settings need an AdminSettings backend model before anything in
 * AUTOMATIZACIÓN can be rendered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigScreen(
    component: AdminConfigComponent,
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
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Lucide.Settings,
                        contentDescription = null,
                        tint = colors.navy,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Configuración",
                        style = typography.displayXSmall,
                        color = colors.text,
                    )
                }
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                ) {
                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
                    AppButton(
                        text = "Reintentar",
                        onClick = component::onRetry,
                        variant = AppButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            else -> {
                ConfigContent(
                    twoFactorStatus = state.twoFactorStatus,
                    onOpenSecurity = component::onOpenSecurity,
                )
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun ConfigContent(
    twoFactorStatus: TwoFactorStatus?,
    onOpenSecurity: () -> Unit,
) {
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        // ── GENERAL ───────────────────────────────────────────────────────────
        SectionHeader(title = "GENERAL", modifier = Modifier.padding(horizontal = 4.dp))

        ConfigCard {
            TwoFactorRow(status = twoFactorStatus, onClick = onOpenSecurity)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── AUTOMATIZACIÓN ────────────────────────────────────────────────────
        // TODO: real settings need an AdminSettings backend model.
        // Toggles are NOT rendered because there is no API to persist them.
        // When the backend supports automation settings (auto-release delay,
        // no-show window, etc.), add real rows here.
        SectionHeader(title = "AUTOMATIZACIÓN", modifier = Modifier.padding(horizontal = 4.dp))

        InfoBanner(
            title = "No disponible aún",
            description = "Las configuraciones de automatización (auto-liberación, " +
                "ventana de no-show, etc.) se activarán cuando el backend " +
                "exponga un modelo AdminSettings.",
            tone = InfoBannerTone.Info,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ConfigCard(content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shape = RoundedCornerShape(dimens.radiusMd)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 16.dp),
    ) {
        content()
    }
}

@Composable
private fun TwoFactorRow(
    status: TwoFactorStatus?,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Lucide.Shield,
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Verificación en dos pasos",
                color = colors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            if (status != null) {
                Text(
                    text = when {
                        status.enforced -> "Obligatorio para administradores"
                        status.enabled  -> "Activo · App TOTP"
                        else            -> "No configurado"
                    },
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
        }

        // Badge showing enforcement/enabled state
        if (status != null) {
            when {
                status.enforced -> AppBadge(text = "Obligatorio", tone = AppBadgeTone.Warning)
                status.enabled  -> AppBadge(text = "Activo", tone = AppBadgeTone.Success)
                else            -> AppBadge(text = "Inactivo", tone = AppBadgeTone.Neutral)
            }
        }

        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(18.dp),
        )
    }
}
