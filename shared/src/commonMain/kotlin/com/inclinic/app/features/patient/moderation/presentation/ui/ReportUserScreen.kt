package com.inclinic.app.features.patient.moderation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ChipSpecialty
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.atoms.AppAvatar

/** Design ref: pencil node YHBAX — "Patient - Reportar Usuario". */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportUserScreen(component: ReportUserComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = com.inclinic.app.ui.theme.AppTheme.colors

    Column(modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            title = { Text("Reportar usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            windowInsets = WindowInsets(0),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // User card — avatar circle + name + specialty
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppAvatar(
                    name = state.targetUserName,
                    size = 44.dp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = state.targetUserName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.text,
                    )
                    Text(
                        text = "Doctor",
                        fontSize = 12.sp,
                        color = colors.muted,
                    )
                }
            }

            // Category chips — Spam / Abuso / Fraude / Otro
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Motivo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportCategory.entries.forEach { category ->
                        ChipSpecialty(
                            label = category.displayLabel(),
                            selected = state.selectedCategory == category,
                            onClick = { component.onCategorySelected(category) },
                        )
                    }
                }
            }

            // Reason text area
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Describe lo sucedido",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                    )
                    Text(
                        text = "${state.reason.length} / 10 mín",
                        fontSize = 10.sp,
                        color = if (state.reason.length >= 10) colors.green else colors.muted,
                        fontWeight = FontWeight.Medium,
                    )
                }
                BasicTextField(
                    value = state.reason,
                    onValueChange = component::onReasonChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.elevated)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (state.reason.isEmpty()) {
                                Text(
                                    text = "Cuéntanos qué pasó (mínimo 10 caracteres)…",
                                    color = colors.light,
                                    fontSize = 14.sp,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            // Confidentiality banner
            InfoBanner(
                title = "Reporte confidencial",
                description = "Nuestro equipo lo revisará en 24–48 horas.",
                tone = InfoBannerTone.Info,
            )

            // Inline error
            state.error?.let { err ->
                Text(
                    text = err,
                    color = colors.red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            val canSubmit = state.reason.length >= 10 && !state.isLoading
            AppButton(
                text = "Enviar reporte",
                onClick = component::onSubmit,
                variant = AppButtonVariant.Navy,
                size = AppButtonSize.Lg,
                loading = state.isLoading,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun ReportCategory.displayLabel(): String = when (this) {
    ReportCategory.Spam -> "Spam"
    ReportCategory.Abuse -> "Abuso"
    ReportCategory.Fraud -> "Fraude"
    ReportCategory.Other -> "Otro"
}
