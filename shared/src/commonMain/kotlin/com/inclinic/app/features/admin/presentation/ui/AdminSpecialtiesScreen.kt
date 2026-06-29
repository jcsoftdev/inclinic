package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Stethoscope
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyItem
import com.inclinic.app.features.admin.presentation.component.AdminSpecialtiesComponent
import com.inclinic.app.features.admin.presentation.component.AdminSpecialtiesFilter
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSpecialtiesScreen(component: AdminSpecialtiesComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = modifier.fillMaxSize().background(colors.sand),
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        "Especialidades",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    // Solicitudes affordance — icon in header
                    Icon(
                        Lucide.ClipboardList,
                        contentDescription = "Solicitudes",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(22.dp)
                            .clickable { component.onOpenRequests() },
                    )
                    Icon(
                        Lucide.Plus,
                        contentDescription = "Crear especialidad",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp)
                            .clickable { component.onShowCreateDialog() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            if (state.isLoading && state.allItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                return@PullToRefreshBox
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = dimens.spacingMd,
                    end = dimens.spacingMd,
                    top = dimens.spacing12,
                    bottom = dimens.spacingLg,
                ),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            ) {
                item {
                    FilterChipRow(
                        options = AdminSpecialtiesFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminSpecialtiesFilter.entries.first { it.label == label }
                            component.onFilterChange(filter)
                        },
                    )
                }

                state.error?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = colors.red,
                            style = AppTheme.typography.body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.radius))
                                .background(colors.redBg)
                                .padding(dimens.spacing12),
                        )
                    }
                }

                if (state.isGapFilter) {
                    item {
                        InfoBanner(
                            title = "Datos no disponibles",
                            description = when (state.activeFilter) {
                                AdminSpecialtiesFilter.UnderReview ->
                                    "Las solicitudes de nuevas especialidades están en la sección Solicitudes (ícono superior). El catálogo no expone un estado 'en revisión'."
                                AdminSpecialtiesFilter.Hidden ->
                                    "El endpoint público solo devuelve especialidades activas. Un endpoint admin dedicado sería necesario para listar las ocultas."
                                else -> ""
                            },
                            tone = InfoBannerTone.Warning,
                            modifier = Modifier.padding(top = dimens.spacingSm),
                        )
                    }
                } else if (state.visibleItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Lucide.Stethoscope,
                                title = "Sin especialidades",
                                subtitle = "No hay especialidades que mostrar.",
                            )
                        }
                    }
                }

                items(state.visibleItems, key = { it.id }) { item ->
                    SpecialtyListRow(item = item)
                }
            }
        }
    }

    // Create specialty dialog
    if (state.showCreateDialog) {
        CreateSpecialtyDialog(
            isCreating = state.isCreating,
            error = state.createError,
            onDismiss = component::onDismissCreateDialog,
            onCreate = { name, desc, icon -> component.onCreateSpecialty(name, desc, icon) },
        )
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun SpecialtyListRow(
    item: AdminSpecialtyItem,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        // Initials avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.navyTint),
        ) {
            Text(
                text = item.initials,
                color = colors.navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            if (!item.description.isNullOrBlank()) {
                Text(
                    item.description,
                    fontSize = 11.sp,
                    color = colors.muted,
                    maxLines = 1,
                )
            }
        }

        Icon(
            Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.light,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun CreateSpecialtyDialog(
    isCreating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String?, icon: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = {
            Text("Nueva especialidad", style = AppTheme.typography.titleLarge, color = colors.text)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing12)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre *",
                    placeholder = "Ej. Cardiología",
                    error = error?.takeIf { name.isBlank() },
                )
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    placeholder = "Opcional",
                )
                AppTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = "Ícono",
                    placeholder = "Nombre o URL del ícono (opcional)",
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = colors.red,
                        fontSize = 12.sp,
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Crear",
                onClick = { onCreate(name, description.takeIf { it.isNotBlank() }, icon.takeIf { it.isNotBlank() }) },
                loading = isCreating,
                enabled = name.isNotBlank() && !isCreating,
                size = AppButtonSize.Sm,
            )
        },
        dismissButton = {
            TextButton(onClick = { if (!isCreating) onDismiss() }) {
                Text("Cancelar", color = colors.muted)
            }
        },
        containerColor = colors.surface,
    )
}
