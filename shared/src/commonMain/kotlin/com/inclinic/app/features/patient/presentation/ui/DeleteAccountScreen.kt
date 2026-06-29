package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CalendarX
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.UserX
import com.inclinic.app.features.patient.presentation.component.DeleteAccountComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme

/**
 * Full-screen "Eliminar cuenta" confirmation — design node PR3DC.
 *
 * Separate screen (not a dialog) with: danger banner, consequence list,
 * password field, and destructive confirm button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    component: DeleteAccountComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Eliminar cuenta",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Scrollable body ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Error banner (API errors)
                state.error?.let {
                    ErrorBanner(message = it, onDismiss = component::onDismissError)
                }

                // Permanent danger warning (design: red banner with triangle-alert)
                InfoBanner(
                    title = "Acción permanente",
                    description = "Esta acción no se puede deshacer.",
                    tone = InfoBannerTone.Error,
                )

                // Consequence list card
                ConsequenceCard()

                // Password confirmation
                OutlinedTextField(
                    value = state.password,
                    onValueChange = component::onPasswordChange,
                    label = { Text("Confirma tu contraseña") },
                    singleLine = true,
                    enabled = !state.isDeleting,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = state.error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Sticky CTA ────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = "Eliminar mi cuenta",
                    onClick = component::onConfirm,
                    enabled = !state.isDeleting && state.password.isNotBlank(),
                    loading = state.isDeleting,
                    variant = AppButtonVariant.Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ConsequenceCard() {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ConsequenceRow(icon = Lucide.UserX, text = "Se eliminan tus datos personales y perfil")
        ConsequenceRow(icon = Lucide.CalendarX, text = "Tus citas activas se cancelan")
        ConsequenceRow(icon = Lucide.Scale, text = "Conservamos registros exigidos por ley (Ley 29733)")
    }
}

@Composable
private fun ConsequenceRow(icon: ImageVector, text: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            color = colors.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}
