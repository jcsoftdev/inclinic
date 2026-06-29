package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Send
import com.inclinic.app.core.model.NegotiationProposal
import com.inclinic.app.core.model.NegotiationStatus
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.patient.presentation.component.NegotiationComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun NegotiationScreen(
    component: NegotiationComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        NegotiationAppHeader(
            title = "Negociación Paquete",
            onBack = component::onBack,
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            state.error != null && state.negotiation == null && state.proposedPrice.isEmpty() -> {
                ErrorBanner(message = state.error!!, onDismiss = component::onErrorDismissed)
            }
            else -> {
                val negotiation = state.negotiation

                negotiation?.let { NegotiationHeaderCard(it) }

                // Non-blocking error
                state.error?.let {
                    ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
                }

                negotiation?.let { StatusBanner(it.status) }

                // Proposals thread
                val proposals = negotiation?.proposals ?: emptyList()
                val listState = rememberLazyListState()
                LaunchedEffect(proposals.size) {
                    if (proposals.isNotEmpty()) {
                        listState.animateScrollToItem(proposals.lastIndex)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(colors.sand),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { Spacer(Modifier.height(0.dp)) }
                    items(proposals, key = { it.id }) { proposal ->
                        ProposalBubble(
                            proposal = proposal,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    item { Spacer(Modifier.height(0.dp)) }
                }

                NegotiationFooter(
                    negotiation = negotiation,
                    proposedPrice = state.proposedPrice,
                    proposedSessions = state.proposedSessions,
                    messageText = state.messageText,
                    isSending = state.isSending,
                    onPriceChange = component::onProposedPriceChange,
                    onSessionsChange = component::onProposedSessionsChange,
                    onMessageChange = component::onMessageChange,
                    onSubmit = component::onSubmitProposal,
                    onAccept = component::onAccept,
                    onReject = component::onReject,
                    onPay = component::onPay,
                )
            }
        }
    }
}

// ── App Header ────────────────────────────────────────────────────────────────

@Composable
private fun NegotiationAppHeader(title: String, onBack: () -> Unit) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Lucide.ArrowLeft,
                contentDescription = "Volver",
                tint = colors.text,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = title,
            style = typography.body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

// ── Header Card ─────────────────────────────────────────────────────────────--

@Composable
private fun NegotiationHeaderCard(negotiation: PackageNegotiation) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val statusUi = negStatusUi(negotiation.status, colors)

    val title = negotiation.offerName ?: "Negociación de paquete"
    val initials = negotiation.doctorName
        ?.split(" ")
        ?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("")
        ?: title.take(2).uppercase()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(0.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.purple),
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = colors.text,
            )
            negotiation.doctorName?.let {
                Text(
                    text = "con $it",
                    style = typography.body.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = colors.muted,
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusUi.background)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = statusUi.label,
                color = statusUi.foreground,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Status banner ─────────────────────────────────────────────────────────────

@Composable
private fun StatusBanner(status: NegotiationStatus) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val message = when (status) {
        NegotiationStatus.PENDING_DOCTOR -> "Esperando al doctor"
        NegotiationStatus.PENDING_PATIENT -> "Tu turno"
        NegotiationStatus.ACCEPTED -> "Aceptada"
        NegotiationStatus.REJECTED -> "Rechazada"
        NegotiationStatus.EXPIRED -> "Expirada"
        NegotiationStatus.PAID -> "Pagada"
    }
    val statusUi = negStatusUi(status, colors)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(statusUi.background)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = message,
            style = typography.body.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            color = statusUi.foreground,
        )
    }
}

// ── Proposal Bubble ─────────────────────────────────────────────────────────--
// Patient (right): tealBg + teal border. Doctor (left): surface.

@Composable
private fun ProposalBubble(
    proposal: NegotiationProposal,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val isPatient = proposal.proposedBy.uppercase() == "PATIENT"
    val alignment = if (isPatient) Alignment.End else Alignment.Start

    val local = proposal.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "${local.hour}:${local.minute.toString().padStart(2, '0')}"

    Column(
        horizontalAlignment = alignment,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = if (isPatient) "Tú propusiste" else "El doctor propuso",
            style = typography.body.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            ),
            color = colors.muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        val bubbleModifier = if (isPatient) {
            Modifier
                .widthIn(max = 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.tealBg)
                .border(1.dp, colors.teal, RoundedCornerShape(16.dp))
                .padding(12.dp)
        } else {
            Modifier
                .widthIn(max = 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(12.dp)
        }

        Box(modifier = bubbleModifier) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "S/. ${proposal.pricePerSession.formatDecimal(2)}/sesión · ${proposal.sessions} sesiones",
                    style = typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                )
                proposal.message?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = typography.body.copy(fontSize = 13.sp),
                        color = colors.text,
                    )
                }
            }
        }

        Text(
            text = timeStr,
            style = typography.body.copy(fontSize = 10.sp),
            color = if (isPatient) colors.teal else colors.muted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// ── Footer ──────────────────────────────────────────────────────────────────--

@Composable
private fun NegotiationFooter(
    negotiation: PackageNegotiation?,
    proposedPrice: String,
    proposedSessions: String,
    messageText: String,
    isSending: Boolean,
    onPriceChange: (String) -> Unit,
    onSessionsChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onPay: () -> Unit,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    val isStartMode = negotiation == null
    val status = negotiation?.status
    val canRespond = status == NegotiationStatus.PENDING_PATIENT
    val showForm = isStartMode || canRespond
    val isAccepted = status == NegotiationStatus.ACCEPTED

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(0.dp),
            )
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
    ) {
        when {
            isAccepted -> {
                val total = (negotiation.finalPricePerSession ?: 0.0) * (negotiation.finalSessions ?: 0)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.navy)
                        .clickable(enabled = !isSending, onClick = onPay)
                        .padding(vertical = 14.dp),
                ) {
                    Text(
                        text = "Pagar S/. ${total.formatDecimal(2)}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            showForm -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = proposedPrice,
                        onValueChange = onPriceChange,
                        placeholder = "Precio x sesión",
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        value = proposedSessions,
                        onValueChange = onSessionsChange,
                        placeholder = "Sesiones",
                        modifier = Modifier.weight(1f),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.sand)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicTextField(
                        value = messageText,
                        onValueChange = onMessageChange,
                        singleLine = false,
                        maxLines = 3,
                        textStyle = typography.body.copy(color = colors.text, fontSize = 13.sp),
                        cursorBrush = SolidColor(colors.navy),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (messageText.isEmpty()) {
                                    Text(
                                        text = "Mensaje (opcional)...",
                                        style = typography.body.copy(fontSize = 13.sp),
                                        color = colors.light,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                }

                PrimaryButton(
                    label = if (isStartMode) "Enviar propuesta" else "Contraofertar",
                    isLoading = isSending,
                    onClick = onSubmit,
                )

                if (canRespond) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(
                            label = "Aceptar",
                            color = colors.green,
                            isEnabled = !isSending,
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                        )
                        SecondaryButton(
                            label = "Rechazar",
                            color = colors.error,
                            isEnabled = !isSending,
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            else -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.navyTint)
                        .padding(vertical = 12.dp),
                ) {
                    Text(
                        text = when (status) {
                            NegotiationStatus.PENDING_DOCTOR -> "Esperando respuesta del doctor"
                            NegotiationStatus.REJECTED -> "Negociación rechazada"
                            NegotiationStatus.EXPIRED -> "Negociación expirada"
                            NegotiationStatus.PAID -> "Paquete pagado"
                            else -> "Negociación cerrada"
                        },
                        color = colors.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.sand)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = typography.body.copy(color = colors.text, fontSize = 13.sp),
            cursorBrush = SolidColor(colors.navy),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = typography.body.copy(fontSize = 13.sp),
                            color = colors.light,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.navy)
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    color: Color,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, color, RoundedCornerShape(14.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Status helpers ────────────────────────────────────────────────────────────

private data class NegStatusUi(val label: String, val background: Color, val foreground: Color)

private fun negStatusUi(status: NegotiationStatus, colors: AppColors): NegStatusUi = when (status) {
    NegotiationStatus.PENDING_DOCTOR  -> NegStatusUi("ESPERANDO DOCTOR", colors.amberBg, colors.amber)
    NegotiationStatus.PENDING_PATIENT -> NegStatusUi("TU TURNO", colors.amberBg, colors.amber)
    NegotiationStatus.ACCEPTED        -> NegStatusUi("ACEPTADA", colors.successBg, colors.green)
    NegotiationStatus.REJECTED        -> NegStatusUi("RECHAZADA", colors.errorBg, colors.error)
    NegotiationStatus.EXPIRED         -> NegStatusUi("EXPIRADA", colors.base, colors.muted)
    NegotiationStatus.PAID            -> NegStatusUi("PAGADA", colors.navyTint, colors.navy)
}
