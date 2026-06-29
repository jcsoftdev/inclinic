package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.inclinic.app.features.patient.presentation.component.MessagesListComponent
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.SkeletonMessageRow
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun MessagesListScreen(component: MessagesListComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header — matches design: bold "Mensajes" title, no TopAppBar chrome
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.sand)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
        ) {
            Text(
                text = "Mensajes",
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        state.error?.let { ErrorBanner(message = it, onDismiss = { }) }

        when {
            state.isLoading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Skeleton rows shaped like ConversationRow
                repeat(5) { SkeletonMessageRow() }
            }
            state.conversations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "Sin conversaciones",
                    subtitle = "Aquí aparecerán los mensajes de los doctores que te han atendido.",
                    icon = Lucide.MessageCircle,
                )
            }
            else -> Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(state.conversations, key = { it.id }) { conversation ->
                        ConversationRow(
                            doctorName = conversation.doctorName,
                            doctorInitials = conversation.doctorInitials,
                            lastMessage = conversation.lastMessage,
                            timestamp = conversation.lastMessageAt.toString(),
                            unreadCount = conversation.unreadCount,
                            onClick = { component.onConversationClick(conversation.id) },
                        )
                    }
                }
                // Info banner — inside content area, padding 16dp all sides
                MessagesInfoBanner(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun MessagesInfoBanner(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.navy)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Lucide.Info,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Solo doctores que te han atendido",
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ConversationRow(
    doctorName: String,
    doctorInitials: String,
    lastMessage: String,
    timestamp: String,
    unreadCount: Int,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(colors.elevated)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar circle with initials
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            if (doctorInitials.isNotBlank()) {
                Text(
                    text = doctorInitials,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(
                    Lucide.MessageCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Name + message
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = doctorName,
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = timestamp,
                    color = colors.muted,
                    fontSize = 11.sp,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = lastMessage,
                    color = colors.muted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                // Unread badge — only shown when unreadCount > 0
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.navy),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
