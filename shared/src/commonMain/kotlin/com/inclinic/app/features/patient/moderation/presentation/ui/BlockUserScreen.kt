package com.inclinic.app.features.patient.moderation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.theme.AppTheme

/** Design ref: pencil node fXtI3 — "Patient - Bloquear Usuario". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockUserScreen(component: BlockUserComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            title = { Text("Bloquear usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { AppBackButton(onClick = component::onCancel) },
            windowInsets = WindowInsets(0),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Ban icon circle — redBg background
            AppIconCircle(
                icon = Lucide.Ban,
                bgColor = colors.redBg,
                iconTint = colors.red,
                circleSize = 80.dp,
                iconSize = 40.dp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Heading
            Text(
                text = "¿Bloquear a ${state.targetUserName}?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                textAlign = TextAlign.Center,
            )

            // Consequence subtitle
            Text(
                text = "No podrá enviarte mensajes ni agendar contigo. Podrás desbloquearlo cuando quieras.",
                fontSize = 14.sp,
                color = colors.muted,
                textAlign = TextAlign.Center,
            )

            // Optional reason field
            AppTextField(
                value = state.reason,
                onValueChange = component::onReasonChanged,
                label = "Motivo (opcional)",
                placeholder = "Ej. mensajes insistentes",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
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

        // CTAs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppButton(
                text = "Bloquear",
                onClick = component::onConfirm,
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Lg,
                loading = state.isLoading,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Cancelar",
                onClick = component::onCancel,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Lg,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
