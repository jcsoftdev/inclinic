package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.model.ShareScope
import com.inclinic.app.core.model.ShareStatus
import com.inclinic.app.features.patient.presentation.component.ShareRequestTab
import com.inclinic.app.features.patient.presentation.component.ShareRequestsComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRequestsScreen(component: ShareRequestsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quién Leyó Mi Historia",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.sand)
                .padding(padding),
        ) {
            state.error?.let { ErrorBanner(message = it, onDismiss = { }) }

            // Tab bar
            ShareRequestTabs(
                selected = state.selectedTab,
                onSelected = component::onTabSelected,
            )

            val filtered = state.requests.filter { req ->
                when (state.selectedTab) {
                    ShareRequestTab.PENDING -> req.status == ShareStatus.PENDING
                    ShareRequestTab.ACTIVE -> req.status == ShareStatus.APPROVED
                    ShareRequestTab.HISTORY -> req.status in listOf(
                        ShareStatus.REJECTED, ShareStatus.EXPIRED, ShareStatus.REVOKED,
                    )
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes", color = colors.muted, fontSize = 14.sp)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(filtered, key = { it.id }) { request ->
                        ShareRequestCard(
                            request = request,
                            isSubmitting = state.submittingId == request.id,
                            onOpenDetail = { component.onRequestSelected(request.id) },
                            onInlineApprove = { component.onInlineApprove(request.id) },
                            onInlineReject = { component.onInlineReject(request.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareRequestTabs(
    selected: ShareRequestTab,
    onSelected: (ShareRequestTab) -> Unit,
) {
    val colors = AppTheme.colors
    val tabs = ShareRequestTab.entries.toList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(36.dp))
                    .background(if (isSelected) colors.navy else colors.elevated)
                    .then(
                        if (!isSelected) Modifier.border(1.dp, colors.border, RoundedCornerShape(36.dp))
                        else Modifier,
                    )
                    .clickable { onSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) Color.White else colors.muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ShareRequestCard(
    request: ShareRequest,
    isSubmitting: Boolean,
    onOpenDetail: () -> Unit,
    onInlineApprove: () -> Unit,
    onInlineReject: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .clickable(onClick = onOpenDetail)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top row: avatar + doctor info + status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DoctorAvatar(request.doctorName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = request.doctorName ?: "Doctor",
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                request.specialtyName?.let {
                    Text(text = it, color = colors.muted, fontSize = 12.sp)
                }
            }
            ShareStatusBadge(request.status)
        }

        // MOTIVO
        request.reason?.let { reason ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MOTIVO",
                    color = colors.navy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = reason,
                    color = colors.text,
                    fontSize = 13.sp,
                    lineHeight = 18.2.sp,
                )
            }
        }

        // Meta row: scope pill + expiry/time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScopePill(request.scope)
            val time = relativeTimeLabel(request.requestedAt)
            val expiry = request.expiresAt?.let { expiryLabel(it) }
            Text(
                text = if (expiry != null) "$time · $expiry" else time,
                color = colors.text,
                fontSize = 11.sp,
            )
        }

        // Inline action buttons — only for pending requests
        if (request.status == ShareStatus.PENDING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    text = "Rechazar",
                    onClick = onInlineReject,
                    variant = AppButtonVariant.Outline,
                    loading = isSubmitting,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Aprobar",
                    onClick = onInlineApprove,
                    variant = AppButtonVariant.Navy,
                    loading = isSubmitting,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DoctorAvatar(name: String?) {
    val colors = AppTheme.colors
    val initials = (name ?: "?")
        .split(" ")
        .filter { it.isNotBlank() && it.first().isLetter() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.navy),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScopePill(scope: ShareScope) {
    val colors = AppTheme.colors
    val label = when (scope) {
        ShareScope.FULL_HISTORY -> "HISTORIAL COMPLETO"
        ShareScope.SPECIFIC_RECORDS -> "REGISTROS ESPECÍFICOS"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.blueBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = colors.text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ShareStatusBadge(status: ShareStatus) {
    val colors = AppTheme.colors
    val (bg, fg, label) = when (status) {
        ShareStatus.PENDING -> Triple(colors.amberBg, colors.amber, "PENDIENTE")
        ShareStatus.APPROVED -> Triple(colors.greenBg, colors.green, "ACTIVO")
        ShareStatus.EXPIRED -> Triple(colors.navyTint, colors.muted, "EXPIRADO")
        ShareStatus.REJECTED -> Triple(colors.redBg, colors.red, "RECHAZADO")
        ShareStatus.REVOKED -> Triple(colors.redBg, colors.red, "REVOCADO")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private val ShareRequestTab.label: String
    get() = when (this) {
        ShareRequestTab.PENDING -> "Pendientes"
        ShareRequestTab.ACTIVE -> "Activo"
        ShareRequestTab.HISTORY -> "Historial"
    }
