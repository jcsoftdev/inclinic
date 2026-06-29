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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.inclinic.app.features.doctor.profile.core.model.DoctorReview
import com.inclinic.app.features.doctor.profile.core.model.DoctorReviewsPage
import com.inclinic.app.features.doctor.profile.presentation.component.ReviewsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

/**
 * Reviews screen (C4ULJ1 — Doctor Mis Reseñas).
 *
 * Layout (Pencil):
 *  AppHeader "Mis Reseñas" (with back button visible)
 *  Summary card: big average score + 5-star breakdown bars
 *  Review cards list: avatar initials + name + specialty + stars + comment
 */
@Composable
fun ReviewsScreen(
    component: ReviewsComponent,
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
                        text = "Mis Reseñas",
                        style = typography.titleLarge,
                        color = colors.text,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
                ) {
                    val page = state.page
                    if (page != null) {
                        RatingSummaryCard(page)
                        page.reviews.forEach { review ->
                            ReviewCard(review)
                        }
                        if (page.reviews.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            ) {
                                Text(
                                    text = "Aún no tienes reseñas",
                                    style = typography.body,
                                    color = colors.muted,
                                )
                            }
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
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

@Composable
private fun RatingSummaryCard(page: DoctorReviewsPage) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing20),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd),
    ) {
        // Left: big score
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        ) {
            Text(
                text = "${(page.averageRating * 10).toLong() / 10.0}".let {
                    if (!it.contains('.')) "$it.0" else it
                },
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
            )
            StarRow(stars = page.averageRating.toInt())
            Text(
                text = "${page.totalRatings} reseñas",
                style = typography.caption,
                color = colors.muted,
            )
        }

        // Right: 5-star breakdown bars
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.weight(1f),
        ) {
            val reviewsByRating = page.reviews.groupBy { it.rating }
            (5 downTo 1).forEach { starVal ->
                val count = reviewsByRating[starVal]?.size ?: 0
                val fraction = if (page.totalRatings > 0) count.toFloat() / page.totalRatings else 0f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs + 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "$starVal",
                        style = typography.caption,
                        color = colors.muted,
                        modifier = Modifier.width(10.dp),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.navyTint),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.navy)
                                .padding(vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: DoctorReview) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm + 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd - 2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12 - 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Avatar initials circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.lavLight),
            ) {
                Text(
                    text = review.patientInitials.take(1).uppercase(),
                    style = typography.body,
                    color = colors.navy,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                Text(text = review.patientInitials, style = typography.body, color = colors.text)
                if (review.specialty != null) {
                    Text(text = review.specialty, style = typography.caption, color = colors.muted)
                }
            }
            // Stars
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { i ->
                    Icon(
                        imageVector = Lucide.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Color(0xFFFBBF24) else colors.border,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
        if (!review.comment.isNullOrBlank()) {
            Text(
                text = "\"${review.comment}\"",
                style = typography.body,
                color = colors.text,
            )
        }
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { i ->
            Icon(
                imageVector = Lucide.Star,
                contentDescription = null,
                tint = if (i < stars) Color(0xFFFBBF24) else Color(0xFF3A3F5C),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
