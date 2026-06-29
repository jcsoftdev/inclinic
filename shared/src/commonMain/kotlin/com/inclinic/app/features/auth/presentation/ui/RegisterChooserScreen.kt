package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.HeartPulse
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.User
import com.composables.icons.lucide.UserRound
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.inclinic.app.features.auth.presentation.component.RegisterChooserComponent
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RegisterChooserScreen(
    component: RegisterChooserComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        AuthScaffold(modifier = modifier) {
            // ── Brand ────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 32.dp, end = 32.dp, bottom = 32.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.navy),
                ) {
                    Icon(
                        imageVector        = Lucide.HeartPulse,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(40.dp),
                    )
                }

                Text(
                    text      = "ClinicAI",
                    style     = typography.displayMedium,
                    color     = colors.text,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text      = "¿Cómo quieres registrarte?",
                    style     = typography.body,
                    color     = colors.muted,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Role cards ───────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                RoleCard(
                    icon        = Lucide.UserRound,
                    iconBgColor = colors.teal,
                    title       = "Soy Paciente",
                    subtitle    = "Busca médicos, agenda citas y gestiona tu salud",
                    onClick     = component::onPatientSelected,
                )

                RoleCard(
                    icon        = Lucide.Stethoscope,
                    iconBgColor = colors.navy,
                    title       = "Soy Médico",
                    subtitle    = "Gestiona tu consultorio, agenda y pacientes",
                    onClick     = component::onDoctorSelected,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Footer ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            ) {
                Text(
                    text  = "¿Ya tienes cuenta? ",
                    style = typography.subtitle,
                    color = colors.muted,
                )
                Text(
                    text  = "Inicia sesión",
                    style = typography.link,
                    color = colors.navy,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = component::onLogin,
                    ),
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBgColor),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(24.dp),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier            = Modifier.weight(1f),
        ) {
            Text(
                text  = title,
                style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = colors.text,
            )
            Text(
                text  = subtitle,
                style = typography.subtitle,
                color = colors.muted,
            )
        }

        Icon(
            imageVector        = Lucide.ChevronRight,
            contentDescription = null,
            tint               = colors.muted,
            modifier           = Modifier.size(20.dp),
        )
    }
}
