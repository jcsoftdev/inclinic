package com.inclinic.app.ui.atoms

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.User
import com.inclinic.app.ui.theme.AppTheme

enum class PatientTab {
    Home, Search, Appointments, Messages, Profile;

    fun icon(): ImageVector = when (this) {
        Home         -> Lucide.House
        Search       -> Lucide.Search
        Appointments -> Lucide.Calendar
        Messages     -> Lucide.MessageCircle
        Profile      -> Lucide.User
    }

    fun label(): String = when (this) {
        Home         -> "INICIO"
        Search       -> "BUSCAR"
        Appointments -> "CITAS"
        Messages     -> "MENSAJES"
        Profile      -> "PERFIL"
    }
}

@Composable
fun AppNavBar(
    currentTab: PatientTab,
    onTabSelected: (PatientTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val motion = AppTheme.motion

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navBarBottomSafePadding()
            .padding(top = 8.dp, start = 20.dp, end = 20.dp),
    ) {
        val containerShape = RoundedCornerShape(34.dp)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = containerShape,
                    clip = false,
                    ambientColor = Color(0x18000000),
                    spotColor = Color(0x14000000),
                )
                .clip(containerShape)
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.65f), containerShape)
                .padding(5.dp),
        ) {
            val tabs = PatientTab.entries
            val tabWidth = maxWidth / tabs.size
            val selectedIndex = tabs.indexOf(currentTab).coerceAtLeast(0)
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(
                    durationMillis = 360,
                    easing = motion.easingStandard,
                ),
                label = "nav-tab-indicator-offset",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(29.dp))
                    .background(colors.navy),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                tabs.forEach { tab ->
                    NavTabItem(
                        icon = tab.icon(),
                        label = tab.label(),
                        isActive = tab == currentTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val iconColor = if (isActive) Color.White else colors.muted
    val textColor = if (isActive) Color.White else colors.muted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconColor,
            modifier           = Modifier.size(18.dp),
        )
        Text(
            text          = label,
            color         = textColor,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.sp,
            lineHeight    = 14.sp,
            modifier      = Modifier.padding(top = 3.dp),
        )
    }
}
