package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Paperclip
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.UserRound
import com.inclinic.app.core.model.SenderRole
import com.inclinic.app.features.doctor.presentation.component.DoctorChatComponent
import com.inclinic.app.ui.molecules.ChatBubble
import com.inclinic.app.ui.molecules.ChatInputBar
import com.inclinic.app.ui.theme.AppTheme

/**
 * Doctor-owned conversation screen.
 *
 * Design reference: node z3stx in design.pen (Doctor - Conversacion).
 * Layout: AppHeader Doctor + scrollable message list + input bar.
 * Doctor messages (SenderRole.DOCTOR) are shown right-aligned (isMine=true),
 * patient messages are left-aligned (isMine=false).
 *
 * This replaces the former patient-ChatScreen adapter approach.
 * DoctorChatComponent contract is unchanged.
 */
@Composable
fun DoctorChatScreen(component: DoctorChatComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val listState = rememberLazyListState()

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
        // AppHeader Doctor: back + patient name + avatar (design node hjH51)
        DoctorConversationHeader(
            patientName = state.doctorName.ifBlank { "Paciente" },
            onBack = component::onBack,
        )

        // Polling error banner
        if (state.pollingFailed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.errorBg)
                    .padding(8.dp),
            ) {
                Text(
                    text = "Se perdio la conexion. Reintentando...",
                    color = colors.error,
                    fontSize = 13.sp,
                )
            }
        }

        // Message list (node koUC3 in design)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.sand)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            items(state.messages, key = { it.id }) { message ->
                // Doctor is the current user - doctor messages appear on the right
                val isMine = message.senderRole == SenderRole.DOCTOR
                ChatBubble(
                    text = message.text,
                    isMine = isMine,
                    timestamp = "",
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // Error below messages
        state.error?.let { error ->
            Text(
                text = error,
                color = colors.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        // Input bar (node U4coE in design)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.elevated)
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
                onAttach = {},
            )
        }
    }
}

// ── Doctor conversation header ────────────────────────────────────────────────

@Composable
private fun DoctorConversationHeader(
    patientName: String,
    onBack: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val initials = patientName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")

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
        // Back button (design: ChevronLeft icon, circle bg)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.elevated),
        ) {
            Icon(
                imageVector = Lucide.ChevronLeft,
                contentDescription = "Volver",
                tint = colors.navy,
                modifier = Modifier.size(18.dp),
            )
        }

        // Patient avatar circle with initials
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.lav50),
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.navy,
                )
            } else {
                Icon(
                    imageVector = Lucide.UserRound,
                    contentDescription = null,
                    tint = colors.navy,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Patient name + "Conversacion" label
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = patientName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                text = "Conversacion",
                fontSize = 12.sp,
                color = colors.muted,
            )
        }
    }
}
