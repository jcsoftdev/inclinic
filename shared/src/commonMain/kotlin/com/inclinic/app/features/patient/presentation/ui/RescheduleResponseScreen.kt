package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.presentation.component.RescheduleResponseComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleResponseScreen(component: RescheduleResponseComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            title = { Text("Reagenda Doctor", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            windowInsets = WindowInsets(0),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }

            state.proposal == null -> {
                state.error?.let { ErrorBanner(message = it, onDismiss = {}, modifier = Modifier.padding(16.dp)) }
                    ?: Text("No hay propuesta pendiente", modifier = Modifier.padding(16.dp), color = colors.muted)
            }

            else -> {
                val proposal = state.proposal!!

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.error?.let { ErrorBanner(message = it, onDismiss = {}) }

                    // Purple info banner — design: sfg2r/nDzRG with purpleBg fill
                    val doctorName = proposal.doctorName ?: "El doctor"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.purpleBg)
                            .border(1.dp, colors.purple, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Lucide.CalendarClock, contentDescription = null, tint = colors.purple, modifier = Modifier.size(22.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                            Text("$doctorName propone reagendar", color = colors.purple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Si rechazas, la cita original se mantiene en disputa con el administrador.",
                                color = colors.purple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    // CITA ORIGINAL label
                    RrSectionLabel("CITA ORIGINAL")

                    // Original appointment card (dimmed)
                    OriginalCard(proposal, colors)

                    // Arrow down indicator
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.purpleBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("↓", color = colors.purple, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // NUEVA PROPUESTA label — purple
                    RrSectionLabel("NUEVA PROPUESTA", color = colors.purple)

                    // New proposal card — purple border
                    ProposalCard(proposal, colors)

                    // Reason card
                    if (!proposal.reason.isNullOrBlank()) {
                        ReasonCard(proposal.reason, colors)
                    }
                }

                // Action buttons
                ActionsSection(
                    isResponding = state.isResponding,
                    onAccept = component::onAccept,
                    onReject = component::onReject,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun RrSectionLabel(text: String, color: androidx.compose.ui.graphics.Color = AppTheme.colors.muted) {
    Text(
        text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun OriginalCard(proposal: RescheduleProposal, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.7f)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Doctor row + CANCELAR chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.navy),
                    contentAlignment = Alignment.Center,
                ) {
                    val initials = proposal.doctorName?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }?.take(2)?.joinToString("") ?: "DR"
                    Text(initials, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(proposal.doctorName ?: "Doctor", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(proposal.specialtyName ?: "", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.redBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("CANCELAR", color = colors.red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Original date + visit type
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                val dateText = proposal.originalStart?.let { formatDateTimeShort(it) } ?: "—"
                Text(dateText, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.Video, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                Text(visitTypeLabel(proposal.visitType), color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProposalCard(proposal: RescheduleProposal, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .border(1.5.dp, colors.purple, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Doctor row + PROPUESTA chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.navy),
                    contentAlignment = Alignment.Center,
                ) {
                    val initials = proposal.doctorName?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }?.take(2)?.joinToString("") ?: "DR"
                    Text(initials, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(proposal.doctorName ?: "Doctor", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(proposal.specialtyName ?: "", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.purpleBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("PROPUESTA", color = colors.purple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Proposed date + visit type
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                Text(
                    formatDateTimeShort(proposal.proposedStart),
                    color = colors.purple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.Video, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                Text(visitTypeLabel(proposal.visitType), color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReasonCard(reason: String, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.MessageSquare, contentDescription = null, tint = colors.navy, modifier = Modifier.size(14.dp))
            Text("Motivo del doctor", color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        }
        Text(
            "\"$reason\"",
            color = colors.text,
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun ActionsSection(
    isResponding: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    colors: AppColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Accept button — navy filled
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (!isResponding) colors.navy else colors.navy.copy(alpha = 0.5f))
                .then(if (!isResponding) Modifier.clickable(onClick = onAccept) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (isResponding) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Aceptar reagenda", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Reject button — destructive outline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, colors.red, RoundedCornerShape(10.dp))
                .background(colors.surface)
                .then(if (!isResponding) Modifier.clickable(onClick = onReject) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text("Rechazar", color = colors.red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatDateTimeShort(instant: kotlin.time.Instant): String {
    val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val hour = ldt.hour
    val minute = ldt.minute
    val amPm = if (hour < 12) "a.m." else "p.m."
    val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "${monthNames[ldt.month.number - 1]} ${ldt.day}, ${h12}:${minute.toString().padStart(2, '0')} $amPm"
}

private fun visitTypeLabel(type: VisitType): String = when (type) {
    VisitType.VIRTUAL -> "Telemedicina"
    VisitType.HOME -> "Visita a domicilio"
    VisitType.CLINIC -> "Presencial"
}
