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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.model.SenderRole
import com.inclinic.app.core.platform.rememberFilePicker
import com.inclinic.app.features.patient.presentation.component.ChatComponent
import com.inclinic.app.ui.molecules.ChatBubble
import com.inclinic.app.ui.molecules.ChatInputBar
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun ChatScreen(component: ChatComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val listState = rememberLazyListState()
    val picker = rememberFilePicker { file -> if (file != null) component.onAttachmentPicked(file) }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Custom header — matches design: back, avatar, name + online, video + more actions
        ChatHeader(
            doctorName = state.doctorName.ifBlank { "Doctor" },
            onBack = component::onBack,
            onReport = { component.onReportUser(state.doctorId, state.doctorName) },
            onBlock = { component.onBlockUser(state.doctorId, state.doctorName) },
        )

        if (state.pollingFailed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.errorBg)
                    .padding(8.dp),
            ) {
                Text("Se perdió la conexión. Reintentando…", color = colors.error, fontSize = 13.sp)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.sand)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(6.dp)) }

            items(state.messages, key = { it.id }) { message ->
                val isMine = message.senderRole == SenderRole.PATIENT
                ChatBubble(
                    text = message.text,
                    isMine = isMine,
                    timestamp = "",
                )
                message.attachments.forEach { url ->
                    ChatAttachmentBubble(url = url, isMine = isMine)
                }
            }

            item { Spacer(Modifier.height(6.dp)) }
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = colors.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (state.pendingAttachments.isNotEmpty()) {
            PendingAttachmentsRow(
                attachments = state.pendingAttachments,
                onRemove = component::onRemovePendingAttachment,
            )
        }

        // Footer input bar — surface bg, top border, matches design
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .border(
                    width = 1.dp,
                    color = colors.border,
                    shape = RoundedCornerShape(0.dp),
                ),
        ) {
            ChatInputBar(
                value = state.inputText,
                onValueChange = component::onInputChange,
                onSend = component::onSend,
                onAttach = { picker.launch() },
            )
        }
    }
}

// ── Chat Header ───────────────────────────────────────────────────────────────

@Composable
private fun ChatHeader(
    doctorName: String,
    onBack: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
) {
    val colors = AppTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    val initials = doctorName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.elevated)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.ChevronLeft,
                contentDescription = "Volver",
                tint = colors.navy,
                modifier = Modifier.size(18.dp),
            )
        }

        // Avatar with initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Name + online indicator
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = doctorName,
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(colors.green),
                )
                Text(
                    text = "En línea",
                    color = colors.green,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Action buttons: video + more
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.elevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.Video,
                    contentDescription = "Videollamada",
                    tint = colors.navy,
                    modifier = Modifier.size(18.dp),
                )
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.elevated)
                        .clickable { menuOpen = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.EllipsisVertical,
                        contentDescription = "Más opciones",
                        tint = colors.navy,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Reportar", color = colors.text) },
                        onClick = {
                            menuOpen = false
                            onReport()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Bloquear", color = colors.error) },
                        onClick = {
                            menuOpen = false
                            onBlock()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatAttachmentBubble(url: String, isMine: Boolean, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.elevated)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable { uriHandler.openUri(url) }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.tealBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    tint = colors.teal,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = fileNameFromUrl(url),
                    color = colors.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Toca para abrir",
                    color = colors.muted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun PendingAttachmentsRow(
    attachments: List<String>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        attachments.forEachIndexed { index, url ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.navyTint)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.tealBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        tint = colors.teal,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = fileNameFromUrl(url),
                    color = colors.navy,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Quitar adjunto", tint = colors.navy)
                }
            }
        }
    }
}

private fun fileNameFromUrl(url: String): String {
    val withoutQuery = url.substringBefore('?')
    val name = withoutQuery.substringAfterLast('/')
    return if (name.isBlank()) "Adjunto" else name
}
