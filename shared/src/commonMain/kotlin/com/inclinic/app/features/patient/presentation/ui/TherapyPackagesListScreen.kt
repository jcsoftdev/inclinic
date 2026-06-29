package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Plus
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.patient.presentation.component.PackagesTab
import com.inclinic.app.features.patient.presentation.component.TherapyPackagesListComponent
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.SkeletonPackageRow
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun TherapyPackagesListScreen(
    component: TherapyPackagesListComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = component::onBack,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Lucide.ArrowLeft,
                        contentDescription = "Volver",
                        tint = colors.text,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "Mis Paquetes",
                    style = typography.body.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, colors.navy, RoundedCornerShape(10.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Comprar paquete de sesiones",
                            onClick = component::onBuyPackage,
                        )
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Lucide.Plus,
                            contentDescription = null,
                            tint = colors.navy,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Comprar",
                            color = colors.navy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Tabs
            PackagesTabs(
                selected = state.selectedTab,
                onSelected = component::onTabChange,
            )

            // Error
            state.error?.let {
                ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
            }

            // Content
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Skeleton rows shaped like PackageCard
                        repeat(3) { SkeletonPackageRow() }
                    }
                    state.packages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "Sin paquetes",
                            subtitle = "Cuando adquieras un paquete de sesiones aparecerá aquí.",
                            icon = Lucide.Package,
                            actionLabel = "Comprar paquete",
                            onAction = component::onBuyPackage,
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(state.packages, key = { it.id }) { pkg ->
                            PackageCard(
                                pkg = pkg,
                                onClick = { component.onPackageTapped(pkg.id) },
                            )
                        }
                        item { Spacer(Modifier.height(4.dp)) }
                    }
                }
            }
        }
    }
}

// ── Tabs ─────────────────────────────────────────────────────────────────────

@Composable
private fun PackagesTabs(
    selected: PackagesTab,
    onSelected: (PackagesTab) -> Unit,
) {
    val colors = AppTheme.colors
    val tabs = PackagesTab.entries.toList()
    val outerShape = RoundedCornerShape(20.dp)
    val tabShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, outerShape, ambientColor = Color(0x0F000000), spotColor = Color(0x0F000000))
            .clip(outerShape)
            .background(AppTheme.colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.55f), outerShape)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(tabShape)
                    .background(if (isSelected) colors.navy else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelected(tab) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) Color.White else colors.muted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                )
            }
        }
    }
}

private val PackagesTab.label: String
    get() = when (this) {
        PackagesTab.ACTIVE -> "Activos"
        PackagesTab.PENDING_PAYMENT -> "Pend. Pago"
        PackagesTab.HISTORY -> "Historial"
    }

// ── Package Card ─────────────────────────────────────────────────────────────

@Composable
private fun PackageCard(
    pkg: TherapyPackage,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val progress = if (pkg.totalSessions > 0) pkg.completedSessions.toFloat() / pkg.totalSessions else 0f
    val statusUi = packageStatusUi(pkg.status, colors)

    // Card with left 4px navy accent strip — matches design
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .clickable(
                role = Role.Button,
                onClickLabel = "Ver detalle del paquete ${pkg.name}",
                onClick = onClick,
            ),
    ) {
        // Left accent strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (pkg.status == PackageStatus.PENDING_PAYMENT) 190.dp else 220.dp)
                .background(colors.navy),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            // Header: name + status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = pkg.name,
                    style = typography.body.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusUi.background)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = statusUi.label,
                        color = statusUi.foreground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Doctor row — avatar initials + name + specialty
            if (pkg.doctorName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.navy)
                            .clearAndSetSemantics {},
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = pkg.doctorName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString(""),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = if (pkg.specialtyName != null) "${pkg.doctorName} · ${pkg.specialtyName}" else pkg.doctorName,
                        style = typography.body.copy(fontSize = 12.sp),
                        color = colors.muted,
                    )
                }
            }

            when (pkg.status) {
                PackageStatus.ACTIVE, PackageStatus.COMPLETED -> {
                    // Progress section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Sesiones: ${pkg.completedSessions} / ${pkg.totalSessions} completadas",
                            fontSize = 11.sp,
                            color = colors.muted,
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.navy,
                            trackColor = colors.navyTint,
                            strokeCap = StrokeCap.Round,
                        )
                    }

                    // Price + discount
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "S/. ${pkg.pricePerSession} por sesión",
                            fontSize = 12.sp,
                            color = colors.muted,
                        )
                        if (pkg.discount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.greenBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "Ahorro ${pkg.discount}%",
                                    color = colors.green,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // CTA
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.navy)
                            .padding(vertical = 12.dp),
                    ) {
                        Text("Agendar Sesión", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                PackageStatus.PENDING_PAYMENT -> {
                    // Pending payment card
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Total prepagado: S/. ${pkg.totalPrice}",
                            style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = colors.text,
                        )
                        Text(
                            text = "${pkg.totalSessions} sesiones · S/. ${pkg.pricePerSession} c/u",
                            fontSize = 11.sp,
                            color = colors.muted,
                        )
                    }

                    if (pkg.paymentDeadline != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Lucide.Clock, contentDescription = null, tint = colors.amber, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Vence pronto",
                                fontSize = 11.sp,
                                color = colors.amber,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.navy)
                            .padding(vertical = 12.dp),
                    ) {
                        Text("Pagar Ahora", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    // Expired / Cancelled
                    Text(
                        text = "S/. ${pkg.totalPrice}",
                        style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = colors.text,
                    )
                }
            }
        }
    }
}

// ── Status UI ────────────────────────────────────────────────────────────────

private data class PackageStatusUi(
    val label: String,
    val background: Color,
    val foreground: Color,
)

private fun packageStatusUi(status: PackageStatus, colors: AppColors): PackageStatusUi = when (status) {
    PackageStatus.ACTIVE -> PackageStatusUi("Activo", colors.successBg, colors.green)
    PackageStatus.PENDING_PAYMENT -> PackageStatusUi("Pend. Pago", colors.amberBg, colors.amber)
    PackageStatus.COMPLETED -> PackageStatusUi("Completado", colors.navyTint, colors.navy)
    PackageStatus.CANCELLED -> PackageStatusUi("Cancelado", colors.errorBg, colors.error)
    PackageStatus.EXPIRED -> PackageStatusUi("Expirado", colors.base, colors.muted)
}
