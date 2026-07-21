package com.inclinic.app.features.doctor.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.UserRound
import com.composables.icons.lucide.Lock
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.ChipSpecialty
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

/**
 * Mi Perfil screen (D8jjf).
 *
 * Layout (Pencil):
 *  AppHeader "Mi Perfil" (no back button, settings nav icon hidden)
 *  ProfileHeroCard (navy/indigo, avatar + name + specialty chip + email)
 *  Config Nav Card ("Ir a Configuración" list row)
 *  Datos Card (DATOS PERSONALES section with name, email, phone rows)
 *  Prof Card (DATOS PROFESIONALES section with specialty, price, modality, bio rows)
 *  SectionLabel "SEGURIDAD"
 *  Seguridad Card (Cambiar contraseña + Cerrar sesión rows)
 */
@Composable
fun MiPerfilScreen(
    component: MiPerfilComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val dimens = AppTheme.dimens
        val typography = AppTheme.typography

        Scaffold(
            containerColor = colors.sand,
            modifier = modifier.fillMaxSize(),
        ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(colors.sand).padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── AppHeader ──────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.sand)
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                ) {
                    Text(
                        text = "Mi Perfil",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = dimens.spacingMd, end = dimens.spacingMd, top = dimens.spacingMd, bottom = dimens.spacing12),
                ) {
                    val profile = state.profile

                    if (profile != null) {
                        // ── ProfileHeroCard ────────────────────────────────────
                        ProfileHeroCard(profile, onViewPublicProfile = component::onNavigatePublicProfile)

                        // ── Config Nav Card ────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                        ) {
                            ProfileNavRow(
                                icon = Lucide.Settings,
                                title = "Ir a Configuración",
                                subtitle = "Notificaciones, pagos, soporte",
                                onClick = component::onNavigateSettings,
                            )
                        }

                        // ── Datos Card (personal data) ─────────────────────────
                        DatosCard(profile)

                        // ── Prof Card (professional data) ──────────────────────
                        ProfCard(profile, component)

                        // ── Section Label: SEGURIDAD ───────────────────────────
                        Text(
                            text = "SEGURIDAD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = colors.muted,
                            modifier = Modifier.padding(top = dimens.spacing12 - 4.dp),
                        )

                        // ── Seguridad Card ─────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface),
                        ) {
                            ProfileNavRow(
                                icon = Lucide.Lock,
                                title = "Cambiar contraseña",
                                subtitle = "",
                                onClick = component::onNavigateChangePassword,
                            )
                            ProfileNavRow(
                                icon = Lucide.LogOut,
                                title = "Cerrar sesión",
                                subtitle = "",
                                iconBg = colors.redBg,
                                onClick = component::onLogout,
                                showChevron = false,
                            )
                        }
                    } else if (state.error != null) {
                        Text(
                            text = state.error ?: "Error desconocido",
                            style = typography.body,
                            color = colors.red,
                            modifier = Modifier.fillMaxWidth().padding(vertical = dimens.spacingMd),
                        )
                        AppButton(
                            text = "Reintentar",
                            onClick = component::onRetry,
                        )
                    }
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
        } // Scaffold
    }
}

@Composable
private fun ProfileHeroCard(
    profile: DoctorProfile,
    onViewPublicProfile: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing20 - 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.navy)
            .padding(20.dp),
    ) {
        // Avatar circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.navyDark),
        ) {
            Icon(Lucide.UserRound, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = profile.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            if (profile.specialties.isNotEmpty()) {
                ChipSpecialty(
                    label = profile.specialties.first().name,
                    modifier = Modifier,
                )
            }
            Text(
                text = profile.email,
                fontSize = 12.sp,
                color = Color(0x80FFFFFF),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable(onClick = onViewPublicProfile),
            ) {
                Icon(Lucide.ExternalLink, contentDescription = null, tint = Color(0x99FFFFFF), modifier = Modifier.size(12.dp))
                Text(text = "Ver perfil público", fontSize = 11.sp, color = Color(0x99FFFFFF))
            }
        }
    }
}

@Composable
private fun DatosCard(profile: DoctorProfile) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface),
    ) {
        DataHeader("DATOS PERSONALES")
        Divider()
        DataRow(label = "Nombre", value = profile.fullName)
        Divider()
        DataRow(label = "Email", value = profile.email)
        Divider()
        DataRow(label = "Teléfono", value = "–")
    }
}

@Composable
private fun ProfCard(
    profile: DoctorProfile,
    component: MiPerfilComponent,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface),
    ) {
        DataHeader("DATOS PROFESIONALES")
        Divider()
        ClickableDataRow(
            label = "Especialidades",
            value = if (profile.specialties.isNotEmpty()) profile.specialties.joinToString { it.name } else "–",
            onClick = component::onNavigateEditSpecialties,
        )
        Divider()
        DataRow(label = "Precio consulta", value = "S/. ${profile.consultationFee.toInt()}")
        Divider()
        DataRow(
            label = "Modalidad",
            value = buildString {
                if (profile.supportsVirtual) append("Teleconsulta")
                if (profile.supportsPresential) {
                    if (isNotEmpty()) append(", ")
                    append("Presencial")
                }
                if (isEmpty()) append("–")
            },
        )
        Divider()
        ClickableDataRow(
            label = "Mis ingresos",
            value = "Ver estado del mes",
            onClick = component::onNavigateIncome,
        )
        Divider()
        ClickableDataRow(
            label = "Mis reseñas",
            value = "Ver reseñas",
            onClick = component::onNavigateReviews,
        )
        Divider()
        ClickableDataRow(
            label = "Mis terapias",
            value = "Gestionar ofertas de terapia",
            onClick = component::onNavigateTherapyOffers,
        )
        Divider()
        ClickableDataRow(
            label = "Cola de no-shows",
            value = "Ver pacientes que no se presentaron",
            onClick = component::onNavigateNoShowQueue,
        )
        Divider()
        ClickableDataRow(
            label = "Citas por cerrar",
            value = "Marcar como completadas o no-show",
            onClick = component::onNavigatePendingClosure,
        )
    }
}

@Composable
private fun DataHeader(title: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd, vertical = dimens.spacingXs + 4.dp),
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.muted)
    }
}

@Composable
private fun Divider() {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().size(height = 1.dp, width = 0.dp).background(colors.border),
    )
}

@Composable
private fun DataRow(label: String, value: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd, vertical = dimens.spacingXs + 4.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 12.sp, color = colors.muted)
            Text(value, fontSize = 14.sp, color = colors.text)
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ClickableDataRow(label: String, value: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingXs + 4.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 12.sp, color = colors.muted)
            Text(value, fontSize = 14.sp, color = colors.text)
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color = AppTheme.colors.surface,
    onClick: () -> Unit,
    showChevron: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingMd, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
        ) {
            Icon(icon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = colors.text)
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = colors.muted)
        }
        if (showChevron) {
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
        }
    }
}
