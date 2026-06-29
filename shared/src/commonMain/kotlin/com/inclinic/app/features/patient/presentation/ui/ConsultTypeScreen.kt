package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.features.patient.presentation.component.ConsultType
import com.inclinic.app.features.patient.presentation.component.ConsultTypeComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun ConsultTypeScreen(component: ConsultTypeComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header: back + title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBackButton(onClick = component::onBack)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "Agendar Cita",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(48.dp))
        }

        // Progress bar — step 1 of 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.navy))
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.border))
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.border))
        }

        Spacer(Modifier.height(4.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Doctor mini card
            state.doctor?.let { DoctorMiniCard(it, colors) }

            // Section label
            Text(
                "TIPO DE CONSULTA",
                color = colors.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )

            // Consult type options
            val doctor = state.doctor
            ConsultTypeOption(
                icon = Lucide.Building2,
                iconBg = colors.navyTint,
                iconColor = colors.navy,
                title = "Consulta presencial",
                subtitle = "Atención en consultorio",
                isSelected = state.selectedType == ConsultType.PRESENCIAL,
                onClick = { component.onTypeSelected(ConsultType.PRESENCIAL) },
                colors = colors,
            )

            if (doctor?.virtualVisitAvailable != false) {
                ConsultTypeOption(
                    icon = Lucide.Video,
                    iconBg = colors.tealBg,
                    iconColor = colors.teal,
                    title = "Telemedicina",
                    subtitle = "Videollamada con tu doctor",
                    isSelected = state.selectedType == ConsultType.TELEMEDICINE,
                    onClick = { component.onTypeSelected(ConsultType.TELEMEDICINE) },
                    colors = colors,
                )
            }

            if (doctor?.homeVisitAvailable == true) {
                ConsultTypeOption(
                    icon = Lucide.MapPin,
                    iconBg = colors.purpleBg,
                    iconColor = colors.purple,
                    title = "A domicilio",
                    subtitle = "El doctor va donde tú estás",
                    isSelected = state.selectedType == ConsultType.HOME_VISIT,
                    onClick = { component.onTypeSelected(ConsultType.HOME_VISIT) },
                    colors = colors,
                )
            }
        }

        // CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.navy)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = component::onContinue),
                contentAlignment = Alignment.Center,
            ) {
                Text("Continuar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }


    }
}

@Composable
private fun DoctorMiniCard(doctor: Doctor, colors: AppColors) {
    val initials = doctor.fullName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.navyLight),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(doctor.fullName, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (doctor.specialties.isNotEmpty()) {
                Text(
                    doctor.specialties.first().name,
                    color = colors.muted,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun ConsultTypeOption(
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.navy else colors.border,
                shape = RoundedCornerShape(16.dp),
            )
            .background(colors.elevated)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = colors.muted, fontSize = 13.sp)
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.navy else colors.elevated)
                .then(
                    if (!isSelected) Modifier.border(2.dp, colors.border, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(Lucide.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
