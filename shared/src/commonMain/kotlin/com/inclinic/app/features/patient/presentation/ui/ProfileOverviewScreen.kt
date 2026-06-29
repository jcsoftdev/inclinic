package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.UserRound
import com.inclinic.app.core.util.formatBirthDate
import com.inclinic.app.features.patient.presentation.component.ProfileOverviewComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOverviewScreen(
    component: ProfileOverviewComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
                windowInsets = WindowInsets(0),
            )
        },
        containerColor = colors.sand,
        modifier = modifier,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = colors.navy,
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.error?.let {
                        ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
                    }

                    AvatarCard(name = state.profile?.name.orEmpty())

                    PersonalDataSummaryCard(
                        name = state.profile?.name.orEmpty().ifBlank { "—" },
                        email = state.profile?.email.orEmpty().ifBlank { "—" },
                        phone = state.profile?.phone.orEmpty().ifBlank { "—" },
                        dateOfBirth = formatBirthDate(state.profile?.dateOfBirth),
                    )

                    ProfileOverviewCard {
                        OverviewActionRow(
                            icon = Lucide.Pencil,
                            iconTint = colors.navy,
                            label = "Editar perfil",
                            labelColor = colors.text,
                            onClick = component::onEditProfile,
                        )
                        OverviewCardDivider()
                        OverviewActionRow(
                            icon = Lucide.Settings,
                            iconTint = colors.navy,
                            label = "Configuración",
                            labelColor = colors.text,
                            onClick = component::onSettings,
                        )
                    }

                    ProfileOverviewCard {
                        OverviewActionRow(
                            icon = Lucide.LogOut,
                            iconTint = colors.red,
                            label = "Cerrar sesión",
                            labelColor = colors.red,
                            onClick = component::onLogout,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarCard(name: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.UserRound,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(name.ifBlank { "—" }, color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PersonalDataSummaryCard(
    name: String,
    email: String,
    phone: String,
    dateOfBirth: String,
) {
    ProfileOverviewCard {
        OverviewSectionHeader("DATOS PERSONALES")
        OverviewCardDivider()
        OverviewInfoRow("Nombre", name)
        OverviewCardDivider()
        OverviewInfoRow("Email", email)
        OverviewCardDivider()
        OverviewInfoRow("Teléfono", phone)
        OverviewCardDivider()
        OverviewInfoRow("Fecha de nacimiento", dateOfBirth)
    }
}

@Composable
private fun ProfileOverviewCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface),
    ) {
        content()
    }
}

@Composable
private fun OverviewSectionHeader(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun OverviewCardDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.border),
    )
}

@Composable
private fun OverviewInfoRow(label: String, value: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = colors.muted, fontSize = 11.sp)
        Text(value, color = colors.text, fontSize = 14.sp)
    }
}

@Composable
private fun OverviewActionRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Text(label, color = labelColor, fontSize = 14.sp)
        }
        Icon(
            Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.light,
            modifier = Modifier.size(16.dp),
        )
    }
}
