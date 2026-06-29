package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.LifeBuoy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.inclinic.app.features.patient.presentation.component.SettingsComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: SettingsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver")
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
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    // ── CUENTA ─────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("CUENTA")

                        SettingsActionRow(
                            label = "Cambiar contraseña",
                            onClick = component::onChangePassword,
                        )

                        // Email row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.surface)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Correo electrónico", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(state.email, color = colors.muted, fontSize = 12.sp)
                            }
                            if (state.emailVerified) {
                                Icon(Lucide.BadgeCheck, contentDescription = "Verificado", tint = colors.green, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // ── PRIVACIDAD ─────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("PRIVACIDAD")

                        SettingsToggleRow(
                            label = "Notificaciones push",
                            subtitle = "Citas, mensajes y recordatorios",
                            checked = state.pushEnabled,
                            onCheckedChange = { component.onPushToggle(it) },
                        )
                        SettingsToggleRow(
                            label = "Compartir datos analíticos",
                            subtitle = "Ayuda a mejorar la app de forma anónima",
                            checked = state.analyticsEnabled,
                            onCheckedChange = { component.onAnalyticsToggle(it) },
                        )
                    }

                    // ── PREMIUM ────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("PREMIUM")
                        if (state.isPremium) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppTheme.colors.surface)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Lucide.Crown, contentDescription = null, tint = colors.amber, modifier = Modifier.size(20.dp))
                                Text("ClinicAI Premium activo", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            PremiumUpsellCard(onSubscribe = component::onSubscribe)
                        }
                    }

                    // ── SOPORTE ────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("SOPORTE")
                        // No navigation callbacks exposed by SettingsComponent → no-op rows.
                        SettingsActionRow(label = "Centro de ayuda", icon = Lucide.LifeBuoy, onClick = {})
                        SettingsActionRow(label = "Términos y privacidad", icon = Lucide.FileText, onClick = {})
                    }

                    // ── ZONA DE PELIGRO ────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("ZONA DE PELIGRO", color = AppTheme.colors.error)
                        DeleteAccountRow(onClick = component::onDeleteAccount)
                    }
                }
            }
        }
    }

}

@Composable
private fun DeleteAccountRow(onClick: () -> Unit) {
    val danger = AppTheme.colors.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.errorBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Lucide.Trash2, contentDescription = null, tint = danger, modifier = Modifier.size(18.dp))
        Box(Modifier.size(10.dp))
        Text("Eliminar mi cuenta", color = danger, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionLabel(text: String, color: Color = AppTheme.colors.muted) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, color = colors.muted, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = colors.navy),
        )
    }
}

@Composable
private fun SettingsActionRow(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
        }
        Text(label, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PremiumUpsellCard(onSubscribe: () -> Unit) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.navy)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.amber),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Crown, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("ClinicAI Premium", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.amber)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("NUEVO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Citas prioritarias, descuentos en paquetes y soporte 24/7.",
            color = colors.navyTint,
            fontSize = 12.sp,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("S/ 29.90", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("/ mes", color = colors.navyTint, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.amber)
                    .clickable(onClick = onSubscribe)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text("Suscribirme", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
