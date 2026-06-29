package com.inclinic.app.features.patient.presentation.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.core.util.formatDecimal
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.User
import com.inclinic.app.core.model.NegotiationStatus
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.features.patient.presentation.component.TherapyOffersComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapyOffersScreen(
    component: TherapyOffersComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Top bar
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    text = "Comprar Paquete",
                    style = typography.body.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                }
            },
            actions = {
                IconButton(onClick = { /* Info */ }) {
                    Icon(Lucide.Info, contentDescription = "Info", tint = colors.navy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            else -> {
                state.error?.let {
                    ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }

                    // Section label
                    item {
                        Text(
                            text = "OFERTAS DISPONIBLES",
                            style = typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.navy,
                            letterSpacing = 0.3.sp,
                        )
                    }

                    // Offers
                    items(state.offers, key = { it.id }) { offer ->
                        OfferCard(
                            offer = offer,
                            onTap = { component.onOfferTapped(offer.id) },
                            onBuy = { component.onBuy(offer.id) },
                            onNegotiate = { component.onNegotiate(offer.id) },
                            isPurchasing = state.purchasingOfferId == offer.id,
                        )
                    }

                    // Active negotiations section
                    if (state.activeNegotiations.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Negociaciones activas",
                                style = typography.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                color = colors.text,
                            )
                        }
                        items(state.activeNegotiations, key = { it.id }) { negotiation ->
                            NegotiationCard(
                                negotiation = negotiation,
                                onClick = { component.onNegotiationTapped(negotiation.id) },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Offer Card ───────────────────────────────────────────────────────────────

@Composable
private fun OfferCard(
    offer: TherapyOffer,
    onTap: () -> Unit,
    onBuy: () -> Unit,
    onNegotiate: () -> Unit,
    isPurchasing: Boolean = false,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x15000000), spotColor = Color(0x15000000))
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onTap)
            .padding(16.dp),
    ) {
        // Doctor info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.navyTint),
            ) {
                Icon(Lucide.User, contentDescription = null, tint = colors.navy, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (offer.doctorName != null) {
                    Text(
                        text = offer.doctorName,
                        style = typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                        color = colors.text,
                    )
                }
                if (offer.specialtyName != null) {
                    Text(
                        text = offer.specialtyName,
                        style = typography.body.copy(fontSize = 11.sp),
                        color = colors.muted,
                    )
                }
            }
        }

        // Package info
        Text(
            text = offer.name,
            style = typography.body.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
        if (offer.description != null) {
            Text(
                text = offer.description,
                style = typography.body.copy(fontSize = 12.sp),
                color = colors.muted,
                maxLines = 2,
            )
        }

        // Sessions + visit types
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.navyTint)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "${offer.sessions} sesiones",
                    color = colors.navy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            offer.visitTypes.forEach { vt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.lav50)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = vt.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = colors.lav,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Price + negotiate button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Text(
                    text = "S/. ${offer.pricePerSession.formatDecimal(2)}/sesión",
                    style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = colors.text,
                )
                if (offer.originalPrice != null && offer.originalPrice > offer.pricePerSession) {
                    Text(
                        text = "S/. ${offer.originalPrice.formatDecimal(2)}",
                        style = typography.body.copy(
                            fontSize = 11.sp,
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = colors.muted,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (offer.isNegotiable) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.navyTint)
                            .clickable(enabled = !isPurchasing, onClick = onNegotiate)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Lucide.MessageCircle, contentDescription = null, tint = colors.navy, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Negociar",
                                color = colors.navy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.navy)
                        .clickable(enabled = !isPurchasing, onClick = onBuy)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Comprar",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

// ── Negotiation Card ─────────────────────────────────────────────────────────

@Composable
private fun NegotiationCard(
    negotiation: PackageNegotiation,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val statusUi = negotiationStatusUi(negotiation.status, colors)
    val amberBorder = colors.warningBorder

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.amberBg)
            .border(1.dp, amberBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Lucide.Clock, contentDescription = null, tint = colors.amber, modifier = Modifier.size(18.dp))
            Text(
                text = "Negociación en curso",
                style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = colors.text,
            )
        }

        // Body
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val offerName = negotiation.offerName ?: "Paquete"
            val title = negotiation.doctorName?.let { "$offerName · $it" } ?: offerName
            Text(
                text = title,
                style = typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                color = colors.text,
            )
            val agreedTotal = negotiation.finalPricePerSession?.let { price ->
                negotiation.finalSessions?.let { sessions -> price * sessions }
            }
            val detail = agreedTotal?.let { "Total acordado: S/. ${it.formatDecimal(2)} · ${statusUi.label}" }
                ?: "En espera de respuesta del doctor"
            Text(
                text = detail,
                style = typography.body.copy(fontSize = 12.sp),
                color = colors.text,
            )
        }

        // Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.amberBg)
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            Text(
                text = statusPillLabel(negotiation.status),
                color = colors.text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
            )
        }
    }
}

private fun statusPillLabel(status: NegotiationStatus): String = when (status) {
    NegotiationStatus.PENDING_DOCTOR -> "PENDIENTE DOCTOR"
    NegotiationStatus.PENDING_PATIENT -> "TU TURNO"
    NegotiationStatus.ACCEPTED -> "ACEPTADA"
    NegotiationStatus.REJECTED -> "RECHAZADA"
    NegotiationStatus.EXPIRED -> "EXPIRADA"
    NegotiationStatus.PAID -> "PAGADA"
}

private data class NegotiationStatusUi(val label: String, val background: Color, val foreground: Color)

private fun negotiationStatusUi(status: NegotiationStatus, colors: AppColors): NegotiationStatusUi = when (status) {
    NegotiationStatus.PENDING_DOCTOR -> NegotiationStatusUi("Pendiente", colors.amberBg, colors.amber)
    NegotiationStatus.PENDING_PATIENT -> NegotiationStatusUi("Tu turno", colors.amberBg, colors.amber)
    NegotiationStatus.ACCEPTED -> NegotiationStatusUi("Aceptada", colors.successBg, colors.green)
    NegotiationStatus.REJECTED -> NegotiationStatusUi("Rechazada", colors.errorBg, colors.error)
    NegotiationStatus.EXPIRED -> NegotiationStatusUi("Expirada", colors.base, colors.muted)
    NegotiationStatus.PAID -> NegotiationStatusUi("Pagada", colors.navyTint, colors.navy)
}
