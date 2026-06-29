package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Flag
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.inclinic.app.features.patient.presentation.component.ConfirmRatingComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmRatingScreen(component: ConfirmRatingComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    // Unified star value — use the minimum filled so the row reflects the most conservative rating
    val unifiedStars = minOf(state.punctuality, state.professionalism, state.empathy)
        .coerceAtLeast(maxOf(state.punctuality, state.professionalism, state.empathy))

    Column(modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            title = { Text("Confirmar Rating", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            windowInsets = WindowInsets(0),
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Amber warning banner — design: sfg2r with amberBg fill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.amberBg)
                        .border(1.dp, colors.amber, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(Lucide.Calendar, contentDescription = null, tint = colors.amber, modifier = Modifier.size(22.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Tienes 18 horas para confirmar", color = colors.amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Si no actúas, liberamos el pago automáticamente con 4★",
                            color = colors.amber,
                            fontSize = 12.sp,
                        )
                    }
                }

                // Doctor mini card — design: Ke5l5 DoctorMiniCard
                val appt = state.appointment
                if (appt != null) {
                    val initials = appt.doctorName?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.uppercase() }
                        ?.take(2)?.joinToString("") ?: "DR"
                    val dt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                    val dateStr = "${dt.day} ${monthNames[dt.month.number - 1]}, ${dt.year}"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.elevated)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.navy),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(initials, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(appt.doctorName ?: "Doctor", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${appt.specialtyName ?: ""} · $dateStr", color = colors.muted, fontSize = 12.sp)
                        }
                    }
                }

                // Rating heading — design uses Funnel Sans 20sp bold
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "¿Cómo estuvo tu consulta?",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Tu calificación libera el pago a la doctora",
                        color = colors.muted,
                        fontSize = 12.sp,
                    )
                }

                // Unified 5-star row — size 40, centered, amber fill
                // Clicking sets all three dimension callbacks simultaneously
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= unifiedStars) colors.amber else colors.border,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    component.onPunctualityChanged(i)
                                    component.onProfessionalismChanged(i)
                                    component.onEmpathyChanged(i)
                                },
                        )
                    }
                }

                // Category tag chips — Puntual / Profesional / Empático
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    listOf("Puntual", "Profesional", "Empático").forEachIndexed { idx, tag ->
                        val tagFilled = when (idx) {
                            0 -> state.punctuality > 0
                            1 -> state.professionalism > 0
                            else -> state.empathy > 0
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.elevated)
                                .border(
                                    width = if (tagFilled) 0.dp else 1.dp,
                                    color = colors.border,
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                tag,
                                color = if (tagFilled) colors.navy else colors.muted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                // Comment field
                BasicTextField(
                    value = state.comment,
                    onValueChange = component::onCommentChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.elevated)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                    decorationBox = { inner ->
                        Box {
                            if (state.comment.isEmpty()) {
                                Text("Cuéntanos cómo fue tu experiencia (opcional)", color = colors.light, fontSize = 14.sp)
                            }
                            inner()
                        }
                    },
                )

                // Error
                state.error?.let { err ->
                    Text(err, color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Actions footer — design: d2aVQ primary button + flag icon link
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(width = 1.dp, color = colors.border)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val canConfirm = state.punctuality > 0 && state.professionalism > 0 && state.empathy > 0 && !state.isSubmitting
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canConfirm) colors.navy else colors.navy.copy(alpha = 0.4f))
                        .then(if (canConfirm) Modifier.clickable(onClick = component::onConfirm) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Confirmar consulta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Dispute link — design: flag icon + text row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = component::onDispute),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Lucide.Flag, contentDescription = null, tint = colors.red, modifier = Modifier.size(14.dp))
                    Text(
                        "Algo no salió bien — Disputar",
                        color = colors.red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
