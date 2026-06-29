package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.composables.icons.lucide.UserRound
import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.model.ShareScope
import com.inclinic.app.features.patient.presentation.component.ActiveAccessesComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAccessesScreen(component: ActiveAccessesComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Accesos a mi historia",
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

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                state.accesses.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ActiveAccessesBanner()
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Ningún profesional tiene acceso activo",
                            color = colors.muted,
                            fontSize = 14.sp,
                        )
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "info-banner") { ActiveAccessesBanner() }
                    items(state.accesses, key = { it.id }) { access ->
                        ActiveAccessCard(
                            access = access,
                            isRevoking = state.revokingId == access.id,
                            onRevoke = { component.onRevoke(access.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveAccessesBanner() {
    InfoBanner(
        title = "Acceso a tu historia clínica",
        description = "Estos profesionales pueden ver tu historia. Revoca cuando quieras.",
        tone = InfoBannerTone.Info,
    )
}

@Composable
private fun ActiveAccessCard(
    access: ShareRequest,
    isRevoking: Boolean,
    onRevoke: () -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar with initials
            val initials = access.doctorName
                ?.split(" ")
                ?.take(2)
                ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                ?.joinToString("") ?: "?"
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.navyTint),
                contentAlignment = Alignment.Center,
            ) {
                if (initials.isNotEmpty() && initials != "?") {
                    Text(
                        text = initials,
                        color = colors.navy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Icon(
                        imageVector = Lucide.UserRound,
                        contentDescription = null,
                        tint = colors.navy,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = access.doctorName?.let { "Dr/a. $it" } ?: "Médico",
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                val scopeLabel = when (access.scope) {
                    ShareScope.FULL_HISTORY -> "Historia completa"
                    ShareScope.SPECIFIC_RECORDS -> "Registros específicos (${access.recordsRequested?.size ?: 0})"
                }
                val expiryLabel = access.expiresAt?.let {
                    val dt = it.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                    "vence ${dt.day} ${monthLabel(dt.month.number)} ${dt.year}"
                } ?: ""
                Text(
                    text = if (expiryLabel.isNotEmpty()) "$scopeLabel · $expiryLabel" else scopeLabel,
                    color = colors.muted,
                    fontSize = 12.sp,
                )
            }
        }

        AppButton(
            text = "Revocar acceso",
            onClick = onRevoke,
            modifier = Modifier.fillMaxWidth(),
            variant = AppButtonVariant.Danger,
            loading = isRevoking,
        )
    }
}

private fun monthLabel(m: Int) = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")[m - 1]
