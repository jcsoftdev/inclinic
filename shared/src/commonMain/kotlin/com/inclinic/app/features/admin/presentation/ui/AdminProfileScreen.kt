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
import androidx.compose.material3.IconButton
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
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Shield
import com.inclinic.app.features.admin.presentation.component.AdminProfileComponent
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.ui.atoms.AppAvatar
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

/**
 * Mi Perfil screen — shows the authenticated admin's real data from /api/users/me.
 *
 * Design node: Ovfa1 (stub replaced with honest content).
 *
 * Data shown:
 * - Avatar with initials (firstName lastName)
 * - Full name + role subtitle with email
 * - Info rows: email, role
 * - Security section: tappable row → Admin Security (2FA) screen
 * - Logout button (Danger) → LogoutUseCase + emits AdminFlowComponent.Output.Logout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    component: AdminProfileComponent,
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
                Text(
                    text = "Mi Perfil",
                    style = typography.displayXSmall,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            actions = {
                // TODO: Edit profile — pencil icon is visual-only; a profile PATCH endpoint
                // is not yet defined in the backend. Wire when /api/users/me PATCH lands.
                IconButton(onClick = {}) {
                    Icon(
                        Lucide.Pencil,
                        contentDescription = "Editar perfil (próximamente)",
                        tint = colors.muted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
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

            state.user != null -> {
                ProfileContent(
                    user = state.user!!,
                    isLoggingOut = state.isLoggingOut,
                    onOpenSecurity = component::onOpenSecurity,
                    onLogout = component::onLogout,
                )
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    user: AuthUser,
    isLoggingOut: Boolean,
    onOpenSecurity: () -> Unit,
    onLogout: () -> Unit,
) {
    val dimens = AppTheme.dimens
    val fullName = "${user.firstName} ${user.lastName}"
    val roleLabel = user.role.displayLabel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        // ── Hero card ─────────────────────────────────────────────────────────
        HeroCard(name = fullName, roleLabel = roleLabel, email = user.email)

        // ── Info rows ─────────────────────────────────────────────────────────
        SectionHeader(title = "INFORMACIÓN", modifier = Modifier.padding(horizontal = 4.dp))

        InfoCard {
            InfoRow(label = "Email", value = user.email)
            RowDivider()
            InfoRow(label = "Rol", value = roleLabel)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Security section ──────────────────────────────────────────────────
        SectionHeader(title = "SEGURIDAD", modifier = Modifier.padding(horizontal = 4.dp))

        SecurityLinkCard(onClick = onOpenSecurity)

        Spacer(modifier = Modifier.height(4.dp))

        // ── Logout ────────────────────────────────────────────────────────────
        AppButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            variant = AppButtonVariant.Danger,
            size = AppButtonSize.Lg,
            loading = isLoggingOut,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HeroCard(name: String, roleLabel: String, email: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shape = RoundedCornerShape(dimens.radiusLarge)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(dimens.spacingMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        AppAvatar(name = name, size = 56.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                text = "$roleLabel · $email",
                fontSize = 12.sp,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
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
private fun InfoRow(label: String, value: String) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = colors.muted, fontSize = 13.sp)
        Text(
            text = value,
            color = colors.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.border),
    )
}

@Composable
private fun SecurityLinkCard(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shape = RoundedCornerShape(dimens.radiusMd)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Lucide.Shield,
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "Verificación en dos pasos",
            color = colors.text,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun UserRole.displayLabel(): String = when (this) {
    UserRole.SUPER_ADMIN -> "Operaciones"
    UserRole.DOCTOR -> "Doctor"
    UserRole.PATIENT -> "Paciente"
}
