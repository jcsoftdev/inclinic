package com.inclinic.app.features.doctor.packages.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.inclinic.app.features.doctor.packages.core.model.PackageStatus
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.presentation.component.PackageListTab
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun PackagesListScreen(component: PackagesListComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Atrás")
            Text(
                text = "Mis Paquetes",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        // Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimens.spacingMd, end = dimens.spacingMd, top = dimens.spacingMd, bottom = dimens.spacingSm),
        ) {
            PackageListTab.entries.forEach { tab ->
                val active = state.selectedTab == tab
                Text(
                    text = tab.label,
                    style = typography.label,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) Color.White else colors.muted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimens.radiusChip))
                        .background(if (active) colors.navy else colors.elevated)
                        .clickable { component.onTabSelected(tab) }
                        .padding(horizontal = dimens.spacing12, vertical = 6.dp),
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }

                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = colors.red, style = typography.body)
                }

                state.visiblePackages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Lucide.Package,
                            contentDescription = null,
                            tint = colors.muted,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(dimens.spacing12))
                        Text(
                            "No hay paquetes en esta sección",
                            color = colors.muted,
                            style = typography.body,
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.visiblePackages, key = { it.id }) { pkg ->
                        PackageRow(pkg = pkg, onClick = { component.onPackageClicked(pkg.id) })
                    }
                    item { Spacer(Modifier.height(dimens.spacingSm)) }
                }
            }
        }
    }
}

/** ListItem Doctor (FE8zR) style row: icon tile + title/subtitle + chevron. */
@Composable
private fun PackageRow(pkg: TherapyPackage, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    val subtitle = when (pkg.status) {
        PackageStatus.PENDING_PAYMENT -> "${pkg.totalSessions} sesiones · Pendiente pago"
        else -> "${pkg.totalSessions} sesiones · ${formatSoles(pkg.expectedIncome)} total"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .clickable(onClick = onClick)
            .padding(dimens.spacingMd),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(dimens.radiusMd))
                .background(colors.navyTint),
        ) {
            Icon(
                imageVector = Lucide.Package,
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = pkg.packageName,
                style = typography.body,
                fontWeight = FontWeight.Bold,
                color = colors.text,
            )
            Text(
                text = subtitle,
                style = typography.subtitle,
                color = colors.muted,
            )
        }
        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(20.dp),
        )
    }
}

internal fun formatSoles(amount: Double): String {
    val rounded = (amount + 0.005)
    val whole = rounded.toLong()
    val cents = ((rounded - whole) * 100).toInt()
    return if (cents == 0) "S/$whole" else "S/$whole.${cents.toString().padStart(2, '0')}"
}
