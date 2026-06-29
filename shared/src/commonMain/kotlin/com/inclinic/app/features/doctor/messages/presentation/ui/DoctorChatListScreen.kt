package com.inclinic.app.features.doctor.messages.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.messages.core.model.ChatThread
import com.inclinic.app.features.doctor.messages.core.port.ThreadFilter
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun DoctorChatListScreen(
    component: DoctorChatListComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing20)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Volver")
            Text(
                text = "Mensajes",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        // Filter pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
        ) {
            FilterPill("Todos", state.activeFilter == ThreadFilter.ALL) { component.onFilterChange(ThreadFilter.ALL) }
            FilterPill(
                "No leídos",
                state.activeFilter == ThreadFilter.UNREAD,
                activeBg = colors.red,
                inactiveBg = colors.redBg,
                inactiveFg = colors.red,
            ) { component.onFilterChange(ThreadFilter.UNREAD) }
        }

        state.error?.let {
            Text(it, color = colors.red, style = typography.subtitle, modifier = Modifier.padding(horizontal = dimens.spacingMd))
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else if (state.threads.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin conversaciones", style = typography.subtitle, color = colors.muted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                items(state.threads, key = { it.id }) { thread ->
                    ChatThreadRow(thread = thread, onClick = { component.onThreadClick(thread.otherPartyId) })
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    activeBg: Color = AppTheme.colors.navy,
    inactiveBg: Color = AppTheme.colors.surface,
    inactiveFg: Color = AppTheme.colors.muted,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = if (selected) Color.White else inactiveFg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radiusPill))
            .background(if (selected) activeBg else inactiveBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun ChatThreadRow(
    thread: ChatThread,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(dimens.radiusLarge), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.lav),
        ) {
            Text(
                text = initials(thread.otherPartyName),
                color = Color.White,
                style = typography.body,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(thread.otherPartyName, style = typography.subtitle, fontWeight = FontWeight.Bold, color = colors.text)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    thread.lastMessage ?: "Sin mensajes",
                    style = typography.subtitle.copy(fontSize = 12.sp),
                    color = if (thread.unread) colors.text else colors.muted,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                if (thread.unread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.navy),
                    )
                }
            }
        }
    }
}

private fun initials(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
