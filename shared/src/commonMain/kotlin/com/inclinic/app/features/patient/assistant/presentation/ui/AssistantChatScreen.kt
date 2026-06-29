package com.inclinic.app.features.patient.assistant.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.X
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.patient.assistant.core.error.AssistantError
import com.inclinic.app.features.patient.assistant.core.error.ext.toUserMessage
import com.inclinic.app.features.patient.assistant.core.model.AssistantMessage
import com.inclinic.app.features.patient.assistant.core.model.ToolName
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatComponent
import com.inclinic.app.features.patient.assistant.presentation.ui.components.BookingFailCard
import com.inclinic.app.features.patient.assistant.presentation.ui.components.BookingSuccessCard
import com.inclinic.app.features.patient.assistant.presentation.ui.components.ChatInputBar
import com.inclinic.app.features.patient.assistant.presentation.ui.components.DisclaimerBanner
import com.inclinic.app.features.patient.assistant.presentation.ui.components.DoctorCardGrid
import com.inclinic.app.features.patient.assistant.presentation.ui.components.MessageBubble
import com.inclinic.app.features.patient.assistant.presentation.ui.components.SlotPicker
import com.inclinic.app.features.patient.assistant.presentation.ui.components.ToolLoadingPill
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.PatientTab
import com.inclinic.app.ui.theme.AppTheme

/**
 * Root Compose UI screen for the AI assistant chat feature.
 *
 * States:
 * - **Disabled** ([AssistantError.Disabled]): full-screen empty state with bot icon,
 *   "Asistente no disponible" copy and "Volver" secondary button.
 * - **Normal / Streaming**: chat layout with header, disclaimer banner, message list,
 *   suggestion chips, and input bar.
 *
 * Streaming is surfaced visually via the [ChatInputBar] `isStreaming` flag:
 * placeholder changes to "Generando respuesta…" and the send button becomes a red stop button.
 *
 * Wrapped in [AppTheme] — sub-components assume theme is provided.
 */
@Composable
fun AssistantChatScreen(
    component: AssistantChatComponent,
    onNavTabSelected: (PatientTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()

        Surface(
            color = AppTheme.colors.sand,
            modifier = modifier.fillMaxSize(),
        ) {
            // ── Disabled full-screen state ────────────────────────────────────
            if (state.error is AssistantError.Disabled) {
                AssistantUnavailableScreen(onBack = component::onErrorDismissed)
                return@Surface
            }

            Column(Modifier.fillMaxSize()) {
                // ── Chat header bar ───────────────────────────────────────────
                ChatHeaderBar(onClose = { onNavTabSelected(PatientTab.Home) })

                // ── Disclaimer ────────────────────────────────────────────────
                DisclaimerBanner(
                    visible = state.disclaimerVisible,
                    onDismiss = component::onDisclaimerDismissed,
                )

                // ── Error banner (non-Disabled errors) ────────────────────────
                state.error?.let { err ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppTheme.dimens.spacingMd, vertical = AppTheme.dimens.spacingXs),
                    ) {
                        ErrorBanner(
                            message = err.toUserMessage(),
                            onDismiss = component::onErrorDismissed,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // Retry button — only for transient errors
                        if (err is AssistantError.Network ||
                            err is AssistantError.Unknown ||
                            err is AssistantError.Validation
                        ) {
                            AppButton(
                                text = "Reintentar",
                                onClick = component::onRetry,
                                variant = AppButtonVariant.Ghost,
                                size = AppButtonSize.Sm,
                            )
                        }
                    }
                }

                // ── Message list ──────────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.dimens.spacingMd),
                    contentPadding = PaddingValues(vertical = AppTheme.dimens.spacingSm),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        when (msg) {
                            is AssistantMessage.User ->
                                MessageBubble(msg)

                            is AssistantMessage.Assistant ->
                                MessageBubble(msg)

                            is AssistantMessage.ToolResultCard -> when (msg.toolName) {
                                ToolName.LIST_SPECIALTIES -> Unit  // silent — no UI card

                                ToolName.SEARCH_DOCTORS -> {
                                    val doctors = parseDoctorList(msg.result)
                                    DoctorCardGrid(
                                        doctors = doctors,
                                        onReserveDoctor = component::onDoctorCardReserve,
                                    )
                                }

                                ToolName.GET_AVAILABILITY -> {
                                    val (date, slots) = parseAvailability(msg.result)
                                    SlotPicker(
                                        date = date,
                                        slots = slots,
                                        onSlotSelected = { time ->
                                            component.onInputChange("Quiero el horario $time")
                                        },
                                    )
                                }

                                ToolName.BOOK_APPOINTMENT -> {
                                    when (val r = parseBookingResult(msg.result)) {
                                        is com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult.Ok ->
                                            BookingSuccessCard(
                                                result = r,
                                                onNavigateToPayment = { component.onNavigateToPayment(r.appointmentId) },
                                            )
                                        is com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult.Failed ->
                                            BookingFailCard(r)
                                    }
                                }
                            }
                        }
                    }

                    // Live streaming bubble
                    if (state.streamingBuffer.isNotEmpty()) {
                        item(key = "streaming") {
                            MessageBubble(
                                AssistantMessage.Assistant(
                                    id = "streaming",
                                    text = state.streamingBuffer,
                                )
                            )
                        }
                    }

                    // Tool loading pill — hidden for silent tools (listSpecialties)
                    state.activeToolCall
                        ?.takeIf { it.toolName != ToolName.LIST_SPECIALTIES }
                        ?.let { active ->
                            item(key = "tool-loading") {
                                ToolLoadingPill(toolName = active.toolName)
                            }
                        }
                }

                // ── Suggestion chips ──────────────────────────────────────────
                SuggestionChipsRow(
                    enabled = !state.isStreaming,
                    onSuggestion = component::onInputChange,
                )

                // ── Input bar ─────────────────────────────────────────────────
                ChatInputBar(
                    value = state.inputText,
                    onValueChange = component::onInputChange,
                    onSend = component::onSend,
                    enabled = !state.isStreaming && state.retryAfterSeconds == null,
                    cooldownSeconds = state.retryAfterSeconds,
                    isStreaming = state.isStreaming,
                    onStop = component::onStop,
                )
            }
        }
    }
}

/**
 * Full-screen "Asistente no disponible" state — maps to design node `pi2SH`.
 *
 * Shown when [AssistantError.Disabled] is in state. Not a routed screen — rendered
 * conditionally inside [AssistantChatScreen] so the component lifecycle is preserved.
 *
 * [onBack] is wired to [AssistantChatComponent.onErrorDismissed] which clears the error
 * and lets normal navigation pop the screen.
 */
@Composable
private fun AssistantUnavailableScreen(
    onBack: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // App header — back button + "Asistente" title (matches AppHeader Patient Normalized)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.navyTint)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Lucide.ChevronLeft,
                    contentDescription = "Volver",
                    tint = colors.navy,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "Asistente",
                color = colors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimens.spacingMd),
            )
        }

        // Centered body — icon + title + subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.sand)
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Bot icon circle (#1A1D2B elevated bg)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.elevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Lucide.Bot,
                    contentDescription = null,
                    tint = colors.navy,
                    modifier = Modifier.size(34.dp),
                )
            }

            Spacer(Modifier.height(dimens.spacing12))

            Text(
                text = "Asistente no disponible",
                color = colors.text,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(dimens.spacingSm))

            Text(
                text = "El asistente de triaje está temporalmente desactivado. Vuelve a intentarlo más tarde.",
                color = colors.muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Bottom CTA — "Volver" secondary button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.sand)
                .padding(start = dimens.spacing20, end = dimens.spacing20, top = dimens.spacingSm, bottom = dimens.spacingLg),
        ) {
            AppButton(
                text = "Volver",
                onClick = onBack,
                variant = AppButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Top header bar: assistant title + 24/7 online indicator + close button.
 * Background: [AppColors.surface] (#12141F dark) with 1dp bottom border.
 */
@Composable
private fun ChatHeaderBar(onClose: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = dimens.spacing20, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Sparkles, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Asistente ClinicAI", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.green),
                )
                Text("Disponible 24/7", color = colors.muted, fontSize = 11.sp)
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.sand)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.X, contentDescription = "Cerrar", tint = colors.text, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Persistent suggestion chips above the input.
 * Style: elevated bg (#1A1D2B), navy border 1dp, pill radius 36dp.
 */
@Composable
private fun SuggestionChipsRow(
    enabled: Boolean,
    onSuggestion: (String) -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val suggestions = listOf("Ver más doctores", "Agendar ahora")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dimens.spacingMd, end = dimens.spacingMd, bottom = dimens.spacingSm),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        suggestions.forEach { label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimens.radiusPill))
                    .background(colors.elevated)
                    .border(
                        width = 1.dp,
                        color = colors.border,
                        shape = RoundedCornerShape(dimens.radiusPill),
                    )
                    .clickable(enabled = enabled) { onSuggestion(label) }
                    .padding(horizontal = dimens.spacing12, vertical = dimens.spacingSm),
            ) {
                Text(label, color = colors.navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
