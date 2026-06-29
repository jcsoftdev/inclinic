package com.inclinic.app.features.doctor.settings.presentation.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.UserMinus
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme

/**
 * Settings screen (vyndO — Doctor Configuración).
 *
 * Layout (Pencil):
 *  AppHeader "Configuración"
 *  PremiumCardCompact (indigo, upgrade CTA)
 *  SectionLabel "NOTIFICACIONES"
 *  3 toggle rows (Nuevas citas, Mensajes del chat, Recordatorios)
 *  SectionLabel "PAGOS"
 *  MercadoPago toggle row
 *  SectionLabel "SOPORTE"
 *  2 nav rows (Ayuda + Términos)
 *  SectionLabel "ZONA DE PELIGRO"
 *  Eliminar cuenta row
 */
@Composable
fun DoctorSettingsScreen(
    component: DoctorSettingsComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val dimens = AppTheme.dimens
        val typography = AppTheme.typography

        Column(modifier = modifier.fillMaxSize().background(Color(0xFF0A0B14))) {
            // ── Header ─────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0B14))
                    .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
            ) {
                AppBackButton(onClick = component::onBack)
                Spacer(Modifier.width(dimens.spacing12))
                Text(
                    text = "Configuración",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEDEFFF),
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
            ) {
                // ── PremiumCardCompact ─────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF5B6CFF))
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF8892C8)),
                    ) {
                        Icon(Lucide.Crown, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Sube a ClinicAI Premium", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("5% comisión en lugar de 15% · S/49.90/mes", fontSize = 11.sp, color = Color(0xFFA8B0E8))
                    }
                    Icon(Lucide.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }

                // ── NOTIFICACIONES ─────────────────────────────────────────────
                SectionLabel("NOTIFICACIONES")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF262A3D), RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1D2B)),
                ) {
                    ToggleRow(
                        icon = Lucide.CalendarPlus,
                        label = "Nuevas citas",
                        checked = state.newAppointmentsEnabled,
                        onCheckedChange = component::onToggleNewAppointments,
                        divider = true,
                    )
                    ToggleRow(
                        icon = Lucide.MessageCircle,
                        label = "Mensajes del chat",
                        checked = state.chatMessagesEnabled,
                        onCheckedChange = component::onToggleChatMessages,
                        divider = true,
                    )
                    ToggleRow(
                        icon = Lucide.Bell,
                        label = "Recordatorios 1h antes",
                        checked = state.appointmentRemindersEnabled,
                        onCheckedChange = component::onToggleAppointmentReminders,
                        divider = false,
                    )
                }

                // ── PAGOS ──────────────────────────────────────────────────────
                SectionLabel("PAGOS")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF262A3D), RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1D2B))
                        .padding(14.dp),
                ) {
                    // MercadoPago logo placeholder
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF00B1EA)),
                    ) {
                        Text("MP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEDEFFF))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("MercadoPago", fontSize = 14.sp, color = Color(0xFFEDEFFF))
                        Text("Comisión actual 15% · plan FREE", fontSize = 11.sp, color = Color(0xFFA2A8C8))
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF34D399),
                            checkedThumbColor = Color.White,
                        ),
                    )
                }

                // ── SOPORTE ────────────────────────────────────────────────────
                SectionLabel("SOPORTE")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF262A3D), RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1D2B)),
                ) {
                    SettingsNavRow(
                        icon = Lucide.Info,
                        title = "Centro de ayuda",
                        subtitle = "Preguntas frecuentes y soporte",
                        divider = true,
                        onClick = {},
                    )
                    SettingsNavRow(
                        icon = Lucide.FileText,
                        title = "Términos y condiciones",
                        subtitle = "",
                        divider = false,
                        onClick = {},
                    )
                }

                // ── ZONA DE PELIGRO ────────────────────────────────────────────
                SectionLabel("ZONA DE PELIGRO")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF262A3D), RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1D2B)),
                ) {
                    SettingsNavRow(
                        icon = Lucide.Trash2,
                        title = "Eliminar cuenta",
                        subtitle = "Esta acción es irreversible",
                        iconBg = Color(0xFF3A1A1F),
                        chevronTint = Color(0xFFFB5E6B),
                        divider = false,
                        onClick = {},
                    )
                }

                // ── Log out button ─────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.radiusMd))
                        .background(Color(0xFF1A0A0A))
                        .border(1.dp, Color(0xFF3A1A1F), RoundedCornerShape(dimens.radiusMd))
                        .clickable(enabled = !state.isLoggingOut, onClick = component::onLogOut)
                        .padding(vertical = 14.dp),
                ) {
                    Icon(Lucide.LogOut, contentDescription = null, tint = Color(0xFFFB5E6B), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(dimens.spacingSm))
                    Text("Cerrar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFB5E6B))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = Color(0xFFA2A8C8),
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    divider: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (divider) Modifier.border(width = 0.dp, color = Color.Transparent)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .size(height = 56.dp, width = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFA2A8C8), modifier = Modifier.size(16.dp))
            Text(label, fontSize = 14.sp, color = Color(0xFFEDEFFF))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFF5B6CFF),
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF262A3D),
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color(0xFF262A3D),
            ),
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    divider: Boolean,
    iconBg: Color = Color(0xFF1A1D2B),
    chevronTint: Color = Color(0xFFA2A8C8),
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF12141F))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFA2A8C8), modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = Color(0xFFEDEFFF))
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = Color(0xFFA2A8C8))
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = chevronTint, modifier = Modifier.size(18.dp))
    }
}
