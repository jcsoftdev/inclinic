package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Users
import com.inclinic.app.features.admin.presentation.component.AdminMasMenuComponent
import com.inclinic.app.features.admin.presentation.component.MasMenuItem
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMasMenuScreen(
    component: AdminMasMenuComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    "Más",
                    style = AppTheme.typography.displayXSmall,
                    fontSize = 22.sp,
                    color = colors.text,
                )
            },
            actions = {
                Icon(
                    Lucide.Settings,
                    contentDescription = "Configuración",
                    tint = colors.navy,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(22.dp)
                        .clickable { component.onSettingsClicked() },
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = dimens.spacingMd,
                end = dimens.spacingMd,
                top = dimens.spacing12,
                bottom = dimens.spacingLg,
            ),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        ) {
            item {
                Text(
                    text = "GESTIÓN DE PLATAFORMA",
                    style = AppTheme.typography.body,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.muted,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            items(state.items, key = { it.name }) { item ->
                MasMenuRow(
                    item = item,
                    onClick = { component.onMenuItemSelected(item) },
                )
            }
        }
    }
}

@Composable
private fun MasMenuRow(
    item: MasMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        // Leading icon in a tinted circle
        val icon = masMenuIcon(item)
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.navyTint),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(20.dp),
            )
        }

        // Title + subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                text = item.subtitle,
                fontSize = 12.sp,
                color = colors.muted,
            )
        }

        // Trailing chevron
        Icon(
            Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.light,
            modifier = Modifier.size(16.dp),
        )
    }
}

private fun masMenuIcon(item: MasMenuItem): ImageVector = when (item) {
    MasMenuItem.Patients      -> Lucide.Users
    MasMenuItem.Specialties   -> Lucide.BookOpen
    MasMenuItem.Reports       -> Lucide.MessageSquare
    MasMenuItem.Reviews       -> Lucide.Star
    MasMenuItem.BlockedEmails -> Lucide.Mail
    MasMenuItem.Subscriptions -> Lucide.CreditCard
    MasMenuItem.Profile       -> Lucide.User
    MasMenuItem.Notifications -> Lucide.Bell
    MasMenuItem.Security      -> Lucide.ShieldCheck
}
