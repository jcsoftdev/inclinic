package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.MailX
import com.composables.icons.lucide.Plus
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem
import com.inclinic.app.features.admin.presentation.component.AdminBlockedEmailsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.molecules.KpiCard
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlockedEmailsScreen(
    component: AdminBlockedEmailsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = modifier.fillMaxSize().background(colors.sand),
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        "Emails bloqueados",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    IconButton(onClick = component::onShowBlockDialog) {
                        Icon(
                            Lucide.Plus,
                            contentDescription = "Bloquear email",
                            tint = colors.navy,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            if (state.isLoading && state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                return@PullToRefreshBox
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = dimens.spacingMd,
                    end = dimens.spacingMd,
                    top = dimens.spacing12,
                    bottom = dimens.spacingLg,
                ),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            ) {
                // ── Metrics row ───────────────────────────────────────────────
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                    ) {
                        KpiCard(
                            label = "${state.distinctDomains} dominios",
                            value = state.totalBlocked.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        // "Intentos últimos 7 días" NOT backed by any field — omitted.
                    }
                }

                // ── Error banner ──────────────────────────────────────────────
                state.error?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = colors.red,
                            style = AppTheme.typography.body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.radius))
                                .background(colors.redBg)
                                .padding(dimens.spacing12),
                        )
                    }
                }

                state.actionError?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = colors.red,
                            style = AppTheme.typography.body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.radius))
                                .background(colors.redBg)
                                .padding(dimens.spacing12),
                        )
                    }
                }

                // ── Empty state ────────────────────────────────────────────────
                if (state.items.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.MailX,
                                title = "Sin emails bloqueados",
                                subtitle = "No hay emails en la lista de bloqueo.",
                            )
                        }
                    }
                }

                // ── Email list ────────────────────────────────────────────────
                items(state.items, key = { it.id }) { item ->
                    BlockedEmailCard(
                        item = item,
                        isActing = state.isActing,
                        onUnblock = { component.onUnblock(item) },
                    )
                }
            }
        }
    }

    // ── Block-email dialog ─────────────────────────────────────────────────────
    if (state.showBlockDialog) {
        BlockEmailDialog(
            isActing = state.isActing,
            error = state.actionError,
            onDismiss = component::onDismissBlockDialog,
            onConfirm = { email, reason, durationDays ->
                component.onBlock(email, reason, durationDays)
            },
        )
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun BlockedEmailCard(
    item: AdminBlockedEmailItem,
    isActing: Boolean,
    onUnblock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Icon(
                Lucide.Mail,
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(20.dp),
            )

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.reasonExcerpt,
                    fontSize = 12.sp,
                    color = colors.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Expiry badge: permanent vs temporal
            AppBadge(
                text = if (item.expiresAt == null) "Permanente" else "Temporal",
                tone = if (item.expiresAt == null) AppBadgeTone.Error else AppBadgeTone.Warning,
            )
        }

        // Expiry date line (only shown for temporal blocks)
        item.expiresAt?.let {
            Text(
                text = "Expira: ${item.expiryLabel}",
                fontSize = 11.sp,
                color = colors.muted,
            )
        }

        Spacer(Modifier.height(2.dp))

        AppButton(
            text = "Desbloquear",
            onClick = onUnblock,
            variant = AppButtonVariant.Ghost,
            size = AppButtonSize.Sm,
            loading = isActing,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BlockEmailDialog(
    isActing: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (email: String, reason: String, durationDays: Int?) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }

    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    val emailValid = email.isNotBlank() && email.contains("@")
    val reasonValid = reason.length >= 10

    AlertDialog(
        onDismissRequest = { if (!isActing) onDismiss() },
        title = {
            Text("Bloquear email", style = AppTheme.typography.titleLarge, color = colors.text)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing12)) {
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email *",
                    placeholder = "usuario@dominio.com",
                    error = if (email.isNotEmpty() && !emailValid) "Ingresa un email válido" else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                AppTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Razón *",
                    placeholder = "Describe el motivo (mín. 10 caracteres)",
                    error = if (reason.isNotEmpty() && !reasonValid) "Mínimo 10 caracteres" else null,
                )
                AppTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { c -> c.isDigit() } },
                    label = "Duración en días (opcional)",
                    placeholder = "Vacío = bloqueo permanente",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = colors.red,
                        fontSize = 12.sp,
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Bloquear",
                onClick = {
                    val days = durationText.toIntOrNull()
                    onConfirm(email.trim(), reason, days)
                },
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Sm,
                loading = isActing,
                enabled = emailValid && reasonValid && !isActing,
            )
        },
        dismissButton = {
            TextButton(onClick = { if (!isActing) onDismiss() }) {
                Text("Cancelar", color = colors.muted)
            }
        },
        containerColor = colors.surface,
    )
}
