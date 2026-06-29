package com.inclinic.app.features.doctor.therapy_offers.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.ChevronRight
import com.inclinic.app.features.doctor.therapy_offers.core.model.TherapyOffer
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun TherapyOffersListScreen(
    component: TherapyOffersListComponent,
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
        // AppHeader Doctor: back + title "Mis Ofertas" + plus action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Atras")
            Text(
                text = "Mis Ofertas",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(dimens.radiusPill))
                    .background(colors.elevated)
                    .clickable(onClick = component::onCreateClicked),
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = "Nueva oferta",
                    tint = colors.navy,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Subtitle
        Text(
            text = "Ofertas que tus pacientes ven y pueden negociar.",
            style = typography.subtitle,
            color = colors.muted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }

                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = colors.red, style = typography.body)
                }

                state.offers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Lucide.Stethoscope,
                            contentDescription = null,
                            tint = colors.muted,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(dimens.spacing12))
                        Text(
                            "Aun no tienes ofertas publicadas",
                            color = colors.muted,
                            style = typography.body,
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(Modifier.height(dimens.spacingXs)) }
                    items(state.offers, key = { it.id }) { offer ->
                        OfferRow(offer = offer)
                    }
                    item { Spacer(Modifier.height(dimens.spacingSm)) }
                }
            }
        }

        // BottomActionBar: "Nueva oferta" primary CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacing12),
        ) {
            AppButton(
                text = "Nueva oferta",
                onClick = component::onCreateClicked,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** ListItem Doctor (FE8zR) style: icon tile + title/subtitle + active chip + chevron. */
@Composable
private fun OfferRow(offer: TherapyOffer) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated),
    ) {
        // Top row: icon + title/subtitle + toggle-like chip + chevron
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(dimens.radiusPill))
                        .background(colors.navyTint),
                ) {
                    Icon(
                        imageVector = Lucide.Stethoscope,
                        contentDescription = null,
                        tint = colors.navy,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = offer.title,
                        style = typography.body,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Text(
                        text = "${offer.totalSessions} sesiones · S/${offer.pricePerSession.toInt()}/sesion",
                        style = typography.subtitle,
                        color = colors.muted,
                    )
                }
                // Active toggle indicator (visual only)
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (offer.isActive) colors.navy else colors.elevated),
                ) {}
            }
            // Chips row: specialty + active status
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (offer.specialtyName.isNotBlank()) {
                    Chip(text = offer.specialtyName, isActive = true)
                }
                Chip(text = if (offer.isActive) "Activa" else "Inactiva", isActive = offer.isActive)
            }
        }
    }
}

@Composable
private fun Chip(text: String, isActive: Boolean) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val bg = if (isActive) colors.tealBg else colors.elevated
    val fg = if (isActive) colors.teal else colors.muted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(text = text, style = typography.label, color = fg)
    }
}
