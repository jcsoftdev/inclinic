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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquareOff
import com.composables.icons.lucide.Star
import com.inclinic.app.features.admin.infrastructure.remote.AdminReviewItem
import com.inclinic.app.features.admin.presentation.component.AdminReviewsComponent
import com.inclinic.app.features.admin.presentation.component.AdminReviewsFilter
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.StarRating
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewsScreen(component: AdminReviewsComponent, modifier: Modifier = Modifier) {
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
                        "Reseñas",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    Icon(
                        Lucide.Star,
                        contentDescription = "Información",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp),
                    )
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
                item {
                    FilterChipRow(
                        options = AdminReviewsFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminReviewsFilter.entries.first { it.label == label }
                            component.onFilterChange(filter)
                        },
                    )
                }

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

                if (state.items.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.MessageSquareOff,
                                title = "Sin reseñas",
                                subtitle = "No hay reseñas que coincidan con el filtro.",
                            )
                        }
                    }
                }

                items(state.items, key = { it.appointmentId }) { review ->
                    ReviewListCard(
                        review = review,
                        isActing = state.isActing,
                        onHide = { component.onShowHideDialog(review) },
                        onUnhide = { component.onUnhide(review) },
                    )
                }
            }
        }
    }

    // Hide-reason dialog
    state.pendingHideItem?.let { item ->
        HideReasonDialog(
            item = item,
            isActing = state.isActing,
            error = state.actionError,
            onDismiss = component::onDismissHideDialog,
            onConfirm = { reason -> component.onHide(item, reason) },
        )
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun ReviewListCard(
    review: AdminReviewItem,
    isActing: Boolean,
    onHide: () -> Unit,
    onUnhide: () -> Unit,
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
            // Initials avatar — patient initials
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.navyTint),
            ) {
                Text(
                    text = review.patient.initials,
                    color = colors.navy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Title: "{patientName} → {doctorName}"
                Text(
                    text = "${review.patient.fullName} → ${review.doctor.fullName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Relative time
                Text(
                    text = review.ageLabel,
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }

            // Status chip
            ReviewVisibilityBadge(isHidden = review.isHidden)
        }

        // Star rating
        StarRating(rating = review.rating, size = 15.dp)

        // Comment (if present)
        review.comment?.let { comment ->
            Text(
                text = "\"$comment\"",
                fontSize = 12.sp,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.sand)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(2.dp))

        if (review.isHidden) {
            AppButton(
                text = "Mostrar",
                onClick = onUnhide,
                variant = AppButtonVariant.Ghost,
                size = AppButtonSize.Sm,
                loading = isActing,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            AppButton(
                text = "Ocultar",
                onClick = onHide,
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Sm,
                loading = isActing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReviewVisibilityBadge(isHidden: Boolean) {
    val (label, tone) = if (isHidden) {
        "Oculta" to AppBadgeTone.Error
    } else {
        "Visible" to AppBadgeTone.Success
    }
    AppBadge(text = label, tone = tone)
}

@Composable
private fun HideReasonDialog(
    item: AdminReviewItem,
    isActing: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val reasonValid = reason.length >= 10

    AlertDialog(
        onDismissRequest = { if (!isActing) onDismiss() },
        title = {
            Text("Ocultar reseña", style = AppTheme.typography.titleLarge, color = colors.text)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing12)) {
                Text(
                    text = "Reseña de ${item.patient.fullName} → ${item.doctor.fullName}",
                    fontSize = 13.sp,
                    color = colors.muted,
                )
                AppTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Razón *",
                    placeholder = "Describe por qué se oculta (mín. 10 caracteres)",
                    error = if (reason.isNotEmpty() && !reasonValid) "Mínimo 10 caracteres" else null,
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
                text = "Ocultar",
                onClick = { onConfirm(reason) },
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Sm,
                loading = isActing,
                enabled = reasonValid && !isActing,
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
