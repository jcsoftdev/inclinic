package com.inclinic.app.features.doctor.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.MonitorSmartphone
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.UserRound
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile
import com.inclinic.app.features.doctor.profile.presentation.component.PublicProfileComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ChipSpecialty
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

/**
 * Public profile screen (o1WrKB — Doctor Mi Perfil Público).
 *
 * Shows a read-only preview of how the doctor's profile appears to patients.
 * Layout (Pencil):
 *  AppHeader "Perfil Público"
 *  InfoBanner — "Vista previa — así te ven los pacientes"
 *  Hero Card (avatar, name, specialty chips, rating row)
 *  Bio Card (about + edit icon)
 *  Details Card (price, modality, location)
 */
@Composable
fun PublicProfileScreen(
    component: PublicProfileComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val dimens = AppTheme.dimens
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize().background(colors.sand)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // ── Header ─────────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                ) {
                    AppBackButton(onClick = component::onBack)
                    Spacer(Modifier.width(dimens.spacing12))
                    Text(
                        text = "Perfil Público",
                        style = typography.titleLarge,
                        color = colors.text,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12 - 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
                ) {
                    // Preview banner
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.radius))
                            .background(colors.surface)
                            .padding(dimens.spacingMd - 2.dp),
                    ) {
                        Icon(Lucide.Eye, contentDescription = null, tint = colors.navy, modifier = Modifier.size(22.dp))
                        Text(
                            text = "Vista previa — así te ven los pacientes",
                            style = typography.body,
                            color = colors.navy,
                        )
                    }

                    val profile = state.profile
                    if (profile != null) {
                        HeroCard(profile)
                        BioCard(profile)
                        DetailsCard(profile)
                    }
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

@Composable
private fun HeroCard(profile: DoctorProfile) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12 - 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing20),
    ) {
        // Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.navy),
        ) {
            Icon(Lucide.UserRound, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Text(
            text = profile.fullName,
            style = typography.titleLarge,
            color = colors.text,
            fontWeight = FontWeight.Bold,
        )

        if (profile.specialties.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                profile.specialties.forEach { specialty ->
                    ChipSpecialty(label = specialty.name)
                }
            }
        }

        // Rating placeholder row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Icon(Lucide.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
            Text(text = "–", style = typography.body, color = colors.text, fontWeight = FontWeight.Bold)
            Text(text = "· consultas aún no registradas", style = typography.body, color = colors.muted)
        }
    }
}

@Composable
private fun BioCard(profile: DoctorProfile) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm + 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd - 2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Acerca de", style = typography.body, color = colors.text, fontWeight = FontWeight.Bold)
            Icon(Lucide.Pencil, contentDescription = "Editar", tint = colors.muted, modifier = Modifier.size(16.dp))
        }
        Text(
            text = profile.bio ?: "Sin descripción aún.",
            style = typography.body,
            color = colors.muted,
        )
    }
}

@Composable
private fun DetailsCard(profile: DoctorProfile) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface),
    ) {
        // Price row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.MonitorSmartphone, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
                Text(text = "Precio consulta", style = typography.body, color = colors.text)
            }
            Text(
                text = "S/. ${profile.consultationFee.toInt()}",
                style = typography.body,
                color = colors.navy,
                fontWeight = FontWeight.Bold,
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd)
                .size(height = 1.dp, width = 0.dp)
                .background(colors.border),
        )

        // Modality row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
        ) {
            Icon(Lucide.MonitorSmartphone, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
            if (profile.supportsVirtual) {
                ChipSpecialty(label = "Teleconsulta")
            }
            if (profile.supportsPresential) {
                ChipSpecialty(label = "Presencial")
            }
        }

        // Location row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
        ) {
            Icon(Lucide.MapPin, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
            Text(text = "Lima, Perú", style = typography.body, color = colors.text)
        }
    }
}
