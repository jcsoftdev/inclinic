package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.inclinic.app.features.patient.presentation.component.ChangePasswordComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * Patient "Cambiar contraseña" screen — mirrors the doctor module's form/success
 * layout but follows the patient module's Scaffold + TopAppBar + [AppBackButton]
 * header convention (see [DeleteAccountScreen]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    component: ChangePasswordComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    // Navigate back automatically after showing the success state briefly.
    LaunchedEffect(state.success) {
        if (state.success) {
            delay(1500L)
            component.onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cambiar contraseña",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
                windowInsets = WindowInsets(0),
            )
        },
        containerColor = colors.sand,
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
            ) {
                when {
                    state.success -> SuccessCard()
                    else -> {
                        // ── Security icon header ───────────────────────────
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.navy.copy(alpha = 0.12f))
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Icon(
                                imageVector = Lucide.ShieldCheck,
                                contentDescription = null,
                                tint = colors.navy,
                                modifier = Modifier.size(30.dp),
                            )
                        }

                        Text(
                            text = "Actualiza tu contraseña de acceso.",
                            style = typography.body,
                            color = colors.muted,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )

                        Spacer(Modifier.height(dimens.spacingXs))

                        // ── Form ──────────────────────────────────────────
                        AppTextField(
                            value = state.currentPassword,
                            onValueChange = component::onCurrentPasswordChange,
                            label = "Contraseña actual",
                            enabled = !state.isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        AppTextField(
                            value = state.newPassword,
                            onValueChange = component::onNewPasswordChange,
                            label = "Nueva contraseña",
                            enabled = !state.isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        AppTextField(
                            value = state.confirmNewPassword,
                            onValueChange = component::onConfirmNewPasswordChange,
                            label = "Confirmar nueva contraseña",
                            enabled = !state.isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // ── Error message ─────────────────────────────────
                        state.error?.let { err ->
                            Text(
                                text = err,
                                style = typography.caption,
                                color = colors.red,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(Modifier.height(dimens.spacingXs))

                        AppButton(
                            text = "Cambiar contraseña",
                            onClick = component::onSubmit,
                            loading = state.isLoading,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

@Composable
private fun SuccessCard() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF14352A)),
        ) {
            Icon(
                imageVector = Lucide.CircleCheck,
                contentDescription = null,
                tint = Color(0xFF34D399),
                modifier = Modifier.size(34.dp),
            )
        }
        Text(
            text = "Contraseña actualizada",
            style = typography.titleLarge,
            color = colors.text,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Tu contraseña se cambió correctamente.",
            style = typography.body,
            color = colors.muted,
        )
    }
}
