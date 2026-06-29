package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.User
import com.inclinic.app.core.model.RecommendedDoctor
import com.inclinic.app.features.patient.presentation.component.SymptomResultsComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun SymptomResultsScreen(component: SymptomResultsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // ── 3-column header ───────────────────────────────────────────────────
        ResultsHeader(
            onBack = component::onBack,
            onEditSymptoms = component::onEditSymptoms,
        )

        state.error?.let { ErrorBanner(message = it, onDismiss = { }) }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Query chip at top — shows symptom text + pencil edit icon
                if (state.symptoms.isNotEmpty()) {
                    item {
                        QueryChip(
                            query = state.symptoms,
                            onEdit = component::onEditSymptoms,
                        )
                    }
                }

                state.analysis?.let { analysis ->
                    item {
                        AnalysisSummaryCard(analysis)
                    }
                }

                // Recommended doctors header
                if (state.doctors.isNotEmpty()) {
                    item {
                        Text(
                            text = "${state.doctors.size} ESPECIALISTAS CERCANOS",
                            color = colors.muted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Doctor cards
                items(state.doctors, key = { it.doctorId }) { doctor ->
                    RecommendedDoctorCard(
                        doctor = doctor,
                        onClick = { component.onViewDoctorProfile(doctor.doctorId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    onBack: () -> Unit,
    onEditSymptoms: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = dimens.spacing20, vertical = 14.dp),
    ) {
        // Back: chevron + "Volver" text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable(onClick = onBack),
        ) {
            Icon(
                imageVector = Lucide.ChevronLeft,
                contentDescription = "Volver",
                tint = colors.navy,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = "Volver",
                color = colors.navy,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Centered title
        Text(
            text = "Resultados IA",
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        // Spacer to balance the back button width
        Box(modifier = Modifier.size(width = 60.dp, height = 1.dp))
    }
}

/**
 * Query chip at the top of results — shows the symptom query text + pencil icon.
 * Tapping the pencil navigates back to edit symptoms.
 */
@Composable
private fun QueryChip(
    query: String,
    onEdit: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Icon(
            imageVector = Lucide.Search,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = query,
            color = colors.muted,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Lucide.Pencil,
            contentDescription = "Editar síntomas",
            tint = colors.navy,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onEdit),
        )
    }
}

/**
 * Analysis summary card — navy background per design `aiCard` node.
 * Shows AI analysis text + specialty chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisSummaryCard(analysis: com.inclinic.app.core.model.SymptomAnalysis) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.navy)
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Header row: sparkles icon + "Análisis de síntomas" + "CuantIF" badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Lucide.Sparkles,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Análisis de síntomas",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            // Severity badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimens.radiusChip))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = dimens.spacingSm, vertical = 3.dp),
            ) {
                Text(
                    text = analysis.severity.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Analysis description
        val description = analysis.possibleCondition
            ?: "Detecté posibles síntomas relacionados con tu descripción."
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 20.sp,
        )

        // Suggested specialties chips
        if (analysis.recommendedSpecialties.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                analysis.recommendedSpecialties.forEach { specialty ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimens.radiusPill))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(specialty, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedDoctorCard(
    doctor: RecommendedDoctor,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top row: avatar + name/specialty + match %
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.navyTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.User, contentDescription = null, tint = colors.navy, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(doctor.name, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(doctor.specialty, color = colors.muted, fontSize = 12.sp)
            }
        }

        // Meta row: distance + rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            doctor.distance?.let { distance ->
                Text(distance, color = colors.muted, fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Lucide.Star, contentDescription = null, tint = colors.amber, modifier = Modifier.size(14.dp))
                Text("${doctor.rating} (${doctor.reviewCount})", color = colors.muted, fontSize = 12.sp)
            }
        }

        // "Ver Perfil" primary button
        AppButton(
            text = "Ver Perfil",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            size = AppButtonSize.Sm,
        )
    }
}
