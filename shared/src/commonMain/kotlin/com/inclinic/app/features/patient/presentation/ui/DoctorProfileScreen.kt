package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.Review
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.patient.presentation.component.DoctorProfileComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.StarRating
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun DoctorProfileScreen(component: DoctorProfileComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand)
    ) {
        Column(Modifier.fillMaxSize()) {
            ProfileHeader(onBack = component::onBack)

            if (state.isLoading && state.doctor == null) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    state.doctor?.let { doctor ->
                        item { Spacer(Modifier.height(4.dp)) }
                        item { DoctorInfoCard(doctor, colors) }
                        if (!doctor.bio.isNullOrBlank()) {
                            item { AboutSection(doctor.bio, colors) }
                        }
                        item { PricingSection(doctor, colors) }
                        if (state.reviews.isNotEmpty()) {
                            item {
                                Text("Reseñas", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            items(state.reviews, key = { it.id }) { review ->
                                ReviewCard(review, colors)
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }

            state.doctor?.let {
                BookingCta(onBook = component::onBookTapped, colors = colors)
            }


        }
    }
}

@Composable
private fun ProfileHeader(onBack: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppBackButton(onClick = onBack)
        Text("Perfil del Doctor", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Share2, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DoctorInfoCard(doctor: Doctor, colors: AppColors) {
    val initials = doctor.fullName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x15000000))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(doctor.fullName, color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (doctor.isVerified) {
                Icon(Lucide.BadgeCheck, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
            }
        }

        if (doctor.specialties.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                doctor.specialties.take(2).forEachIndexed { i, specialty ->
                    val (bg, fg) = if (i == 0) colors.tealBg to colors.teal
                    else colors.infoBg to colors.info
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            specialty.name.uppercase(),
                            color = fg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }
        }

        doctor.ratingAverage?.let { avg ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StarRating(rating = avg.toInt(), size = 18.dp)
                Text(
                    "${avg.formatDecimal(1)} (${doctor.ratingsCount} reseñas)",
                    color = colors.text,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun AboutSection(bio: String, colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Acerca de", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(bio, color = colors.text, fontSize = 13.sp, lineHeight = 19.5.sp)
    }
}

@Composable
private fun PricingSection(doctor: Doctor, colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tarifas", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        PriceRow(Lucide.Building2, "Consulta presencial", "S/. ${doctor.consultationFee.toInt()}", colors)
        if (doctor.virtualVisitAvailable) {
            PriceRow(Lucide.Video, "Telemedicina", "S/. ${doctor.consultationFee.toInt()}", colors)
        }
        if (doctor.homeVisitAvailable) {
            PriceRow(Lucide.House, "Visita a domicilio", "S/. ${(doctor.consultationFee * 1.3).toInt()}", colors)
        }
    }
}

@Composable
private fun PriceRow(icon: ImageVector, label: String, price: String, colors: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
            Text(label, color = colors.text, fontSize = 13.sp)
        }
        Text(price, color = colors.teal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BookingCta(onBook: () -> Unit, colors: AppColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.sand)
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
                .clickable(interactionSource = interactionSource, indication = null, onClick = onBook),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Lucide.CalendarPlus, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("Agendar Cita", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.elevated)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(review.patientName, color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(review.rating) { Icon(Lucide.Star, contentDescription = null, tint = colors.amber, modifier = Modifier.size(12.dp)) }
                repeat(5 - review.rating) { Icon(Lucide.Star, contentDescription = null, tint = colors.light, modifier = Modifier.size(12.dp)) }
            }
        }
        review.comment?.let {
            Text(it, color = colors.muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
