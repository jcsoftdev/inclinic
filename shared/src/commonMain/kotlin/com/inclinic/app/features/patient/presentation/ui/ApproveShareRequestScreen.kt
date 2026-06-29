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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.inclinic.app.core.model.ShareScope
import com.inclinic.app.features.patient.presentation.component.ApproveShareRequestComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveShareRequestScreen(component: ApproveShareRequestComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Solicitud de Acceso",
                        color = colors.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = component::onClose) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        bottomBar = {
            // Sticky bottom action bar — matches design node RKoQ5
            if (state.request != null && !state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .border(
                            width = 1.dp,
                            color = colors.border,
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppButton(
                        text = "Aprobar acceso",
                        onClick = component::onApprove,
                        variant = AppButtonVariant.Navy,
                        size = AppButtonSize.Lg,
                        loading = state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppButton(
                        text = "Rechazar",
                        onClick = component::onReject,
                        variant = AppButtonVariant.Outline,
                        size = AppButtonSize.Lg,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
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
                state.request == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró la solicitud", color = colors.muted, fontSize = 14.sp)
                }
                else -> {
                    val request = state.request!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Icon header — "Un doctor solicita ver tu historia"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.elevated)
                                .padding(horizontal = 0.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Lucide.ShieldCheck,
                                    contentDescription = null,
                                    tint = colors.navy,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Text(
                                text = "Un doctor solicita ver tu historia",
                                color = colors.text,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }

                        // Doctor mini card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.elevated)
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DoctorInitialsAvatar(request.doctorName)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = request.doctorName ?: "Doctor",
                                    color = colors.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                request.specialtyName?.let {
                                    SpecialtyChip(it)
                                }
                            }
                        }

                        // Reason (quoted, italic)
                        request.reason?.let { reason ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.elevated)
                                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "MOTIVO",
                                    color = colors.muted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                )
                                Text(
                                    text = "“$reason”",
                                    color = colors.text,
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.2.sp,
                                )
                            }
                        }

                        // Scope + requested records (read-only)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.elevated)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "ALCANCE",
                                color = colors.muted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                            )
                            ScopeSelector(request.scope)

                            if (request.scope == ShareScope.SPECIFIC_RECORDS) {
                                request.recordsRequested?.takeIf { it.isNotEmpty() }?.let { records ->
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        records.forEach { record ->
                                            RecordRow(record)
                                        }
                                    }
                                }
                            }
                        }

                        // Duration selector
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.elevated)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "DURACIÓN DEL ACCESO",
                                color = colors.muted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                DurationOptions.forEach { (days, label) ->
                                    DurationChip(
                                        label = label,
                                        isSelected = state.selectedDuration == days,
                                        onClick = { component.onDurationSelected(days) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val DurationOptions = listOf(
    7 to "7d",
    30 to "30d",
    60 to "60d",
    90 to "90d",
    180 to "180d",
    365 to "1a",
)

@Composable
private fun DoctorInitialsAvatar(name: String?) {
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
private fun SpecialtyChip(label: String) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.tealBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ScopeSelector(scope: ShareScope) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScopeOption(
            label = "Todo el historial",
            isSelected = scope == ShareScope.FULL_HISTORY,
            modifier = Modifier.weight(1f),
        )
        ScopeOption(
            label = "Registros específicos",
            isSelected = scope == ShareScope.SPECIFIC_RECORDS,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScopeOption(label: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) Modifier.background(colors.navy)
                else Modifier.border(1.dp, colors.border, RoundedCornerShape(8.dp)),
            )
            .padding(vertical = 9.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.muted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecordRow(record: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.sand)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Lucide.FileText,
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = record,
            color = colors.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DurationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) Modifier.background(colors.navy)
                else Modifier.border(1.dp, colors.border, RoundedCornerShape(8.dp)),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
