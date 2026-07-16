package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.X
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.patient.presentation.component.CardType
import com.inclinic.app.features.patient.presentation.component.PaymentMethodChoice
import com.inclinic.app.features.patient.presentation.component.PaymentComponent
import com.inclinic.app.features.patient.presentation.component.PaymentState
import com.inclinic.app.features.patient.presentation.component.PaymentStatus
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun PaymentScreen(component: PaymentComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()

    // Route to the correct payment-state screen based on paymentStatus
    when (state.paymentStatus) {
        PaymentStatus.PENDING ->
            PaymentPendingScreen(state = state, onPayNow = component::onPayNow, onCancel = component::onCancelReservation, modifier = modifier)
        PaymentStatus.PROCESSING ->
            PaymentProcessingScreen(state = state, onGoToAppointments = component::onGoToAppointments, modifier = modifier)
        PaymentStatus.REJECTED ->
            PaymentRejectedScreen(state = state, onRetry = component::onRetryPayment, onChangeCard = component::onChangeCard, modifier = modifier)
        PaymentStatus.FORM ->
            PaymentFormContent(component = component, state = state, modifier = modifier)
    }
}

// ── Pago Pendiente — JbZ0Q ────────────────────────────────────────────────────

@Composable
private fun PaymentPendingScreen(
    state: PaymentState,
    onPayNow: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val appointment = state.appointment
    val doctor = state.doctor
    val total = appointment?.consultationFee ?: doctor?.consultationFee ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Header spacer
        Box(modifier = Modifier.height(48.dp))

        // Countdown timer block
        val secondsRemaining = state.secondsRemaining
        if (secondsRemaining != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Tu reserva expira en",
                    color = colors.muted,
                    fontSize = 13.sp,
                )
                Text(
                    text = formatCountdown(secondsRemaining),
                    color = colors.amber,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // Doctor + appointment summary card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.elevated)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.isSummaryLoading && appointment == null) {
                Text("Cargando cita...", color = colors.muted, fontSize = 13.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.navyLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials(doctor?.fullName ?: "Dr"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = doctor?.fullName ?: "Doctor asignado",
                            color = colors.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = doctor?.specialties?.firstOrNull()?.name ?: visitTypeLabel(appointment?.visitType),
                            color = colors.muted,
                            fontSize = 12.sp,
                        )
                    }
                }
                appointment?.let {
                    val startsAt = it.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    SummaryRow("Fecha", startsAt.date.toString())
                    SummaryRow("Hora", formatHour(startsAt.hour, startsAt.minute))
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("S/ ${total.formatDecimal(2)}", color = colors.navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Amber info banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.amberBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Tienes 30 minutos para completar el pago. Pasado ese tiempo la reserva se liberará.",
                color = colors.amber,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Primary CTA: Pagar ahora
        AppButton(
            text = "Pagar ahora · S/ ${total.formatDecimal(2)}",
            onClick = onPayNow,
            variant = AppButtonVariant.Navy,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Secondary CTA: Cancelar reserva
        AppButton(
            text = "Cancelar reserva",
            onClick = onCancel,
            variant = AppButtonVariant.Outline,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
    }
}

// ── Pago En Proceso — HOhIi ───────────────────────────────────────────────────

@Composable
private fun PaymentProcessingScreen(
    state: PaymentState,
    onGoToAppointments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val appointment = state.appointment
    val doctor = state.doctor
    val total = appointment?.consultationFee ?: doctor?.consultationFee ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Spacer(Modifier.weight(1f))

        // Spinner icon
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = colors.navy,
            strokeWidth = 4.dp,
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Procesando pago",
            color = colors.text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tu pago está siendo revisado. Te notificaremos cuando se confirme.",
            color = colors.muted,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))

        // Amount + doctor label
        if (doctor != null || appointment != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.elevated)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("S/ ${total.formatDecimal(2)}", color = colors.navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(doctor?.fullName ?: "Doctor asignado", color = colors.muted, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        AppButton(
            text = "Volver a mis citas",
            onClick = onGoToAppointments,
            variant = AppButtonVariant.Outline,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
    }
}

// ── Pago Rechazado — MN3gr ────────────────────────────────────────────────────

@Composable
private fun PaymentRejectedScreen(
    state: PaymentState,
    onRetry: () -> Unit,
    onChangeCard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Spacer(Modifier.weight(1f))

        // Red X icon circle
        AppIconCircle(
            icon = Lucide.X,
            bgColor = colors.redBg,
            iconTint = colors.red,
            circleSize = 80.dp,
            iconSize = 36.dp,
        )
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Pago rechazado",
            color = colors.text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No pudimos procesar tu pago. Puedes reintentar o usar otra tarjeta.",
            color = colors.muted,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.weight(1f))

        AppButton(
            text = "Reintentar pago",
            onClick = onRetry,
            variant = AppButtonVariant.Navy,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        AppButton(
            text = "Cambiar tarjeta",
            onClick = onChangeCard,
            variant = AppButtonVariant.Outline,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
    }
}

// ── Card form (existing PaymentScreen body, extracted) ────────────────────────

@Composable
private fun PaymentFormContent(
    component: PaymentComponent,
    state: PaymentState,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val appointment = state.appointment
    val doctor = state.doctor
    val therapyPackage = state.therapyPackage
    val packageTotal = therapyPackage?.let { it.pricePerSession * it.totalSessions }
    val total = appointment?.consultationFee ?: packageTotal ?: doctor?.consultationFee ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBackButton(onClick = component::onBack)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (state.isPackagePayment) "Pagar Paquete" else "Pagar Cita",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.error?.let {
                ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
            }

            if (state.isPackagePayment) {
                if (therapyPackage != null) {
                    PackageSummaryCard(
                        therapyPackage = therapyPackage,
                        isLoading = state.isSummaryLoading,
                    )
                } else {
                    // Package mode but detail not loaded — neutral placeholder, never the
                    // appointment summary (which would show "Doctor asignado / S/ 0.00").
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.elevated)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isSummaryLoading) {
                            CircularProgressIndicator(color = colors.navy, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text("No se pudo cargar el paquete", color = colors.muted, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                PaymentSummaryCard(
                    appointment = appointment,
                    doctor = doctor,
                    isLoading = state.isSummaryLoading,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("MÉTODO DE PAGO")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.elevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PaymentTab(
                        label = "Tarjeta",
                        selected = state.selectedMethod == PaymentMethodChoice.CARD,
                        modifier = Modifier.weight(1f),
                        onClick = { component.onSelectMethod(PaymentMethodChoice.CARD) },
                    )
                    PaymentTab(
                        label = "Yape",
                        selected = state.selectedMethod == PaymentMethodChoice.YAPE,
                        modifier = Modifier.weight(1f),
                        onClick = { component.onSelectMethod(PaymentMethodChoice.YAPE) },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.elevated)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.selectedMethod == PaymentMethodChoice.CARD) {
                PaymentInput(
                    label = "Número de tarjeta",
                    value = state.cardNumber,
                    placeholder = "4242 4242 4242 4242",
                    keyboardType = KeyboardType.Number,
                    leading = {
                        Icon(Lucide.CreditCard, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                    },
                    trailing = cardBrandLabel(state.cardType),
                    onValueChange = component::onCardNumberChange,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentInput(
                        label = "Vencimiento",
                        value = state.expiry,
                        placeholder = "MM/YY",
                        keyboardType = KeyboardType.Number,
                        onValueChange = component::onExpiryChange,
                        modifier = Modifier.weight(1f),
                    )
                    PaymentInput(
                        label = "CVV",
                        value = state.cvv,
                        placeholder = "123",
                        keyboardType = KeyboardType.Number,
                        onValueChange = component::onCvvChange,
                        modifier = Modifier.weight(1f),
                    )
                }

                PaymentInput(
                    label = "Titular",
                    value = state.cardholderName,
                    placeholder = "Juan Pérez",
                    onValueChange = component::onCardholderNameChange,
                )

                PaymentInput(
                    label = "DNI / documento",
                    value = state.docNumber,
                    placeholder = "12345678",
                    keyboardType = KeyboardType.Number,
                    onValueChange = component::onDocNumberChange,
                )
                } else {
                    PaymentInput(
                        label = "Número de celular",
                        value = state.yapePhone,
                        placeholder = "987654321",
                        keyboardType = KeyboardType.Number,
                        onValueChange = component::onYapePhoneChange,
                    )
                    PaymentInput(
                        label = "Código Yape (6 dígitos)",
                        value = state.yapeOtp,
                        placeholder = "123456",
                        keyboardType = KeyboardType.Number,
                        onValueChange = component::onYapeOtpChange,
                    )
                    Text(
                        text = "Abre tu app Yape → \"Aprobar pagos\" → copia el código de 6 dígitos.",
                        color = colors.muted,
                        fontSize = 11.sp,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Lucide.ShieldCheck, contentDescription = null, tint = colors.green, modifier = Modifier.size(14.dp))
                Text(
                    text = "Pago seguro con MercadoPago · Custodia 24h",
                    color = colors.green,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (state.isExpired) colors.muted else colors.navy)
                    .clickable(enabled = !state.isLoading && !state.isExpired, onClick = component::onSubmit),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading ->
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    state.isExpired ->
                        Text(
                            text = "Plazo de pago expirado",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    else ->
                        Text(
                            text = "Pagar S/ ${total.formatDecimal(2)}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                }
            }
        }
    }
}

@Composable
private fun PaymentSummaryCard(appointment: Appointment?, doctor: Doctor?, isLoading: Boolean) {
    val colors = AppTheme.colors
    val total = appointment?.consultationFee ?: doctor?.consultationFee ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.elevated)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isLoading && appointment == null) {
            Text("Cargando cita...", color = colors.muted, fontSize = 13.sp)
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.navyLight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials(doctor?.fullName ?: "Dr"),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = doctor?.fullName ?: "Doctor asignado",
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = doctor?.specialties?.firstOrNull()?.name ?: visitTypeLabel(appointment?.visitType),
                    color = colors.muted,
                    fontSize = 12.sp,
                )
            }
        }

        SummaryRow("Consulta", "S/ ${total.formatDecimal(2)}")
        SummaryRow("Comisión plataforma", "Incluido", mutedValue = true)
        appointment?.let {
            val startsAt = it.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
            SummaryRow("Fecha", startsAt.date.toString(), mutedValue = true)
            SummaryRow("Hora", formatHour(startsAt.hour, startsAt.minute), mutedValue = true)
            SummaryRow("Modalidad", visitTypeLabel(it.visitType), mutedValue = true)
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Total", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("S/ ${total.formatDecimal(2)}", color = colors.navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PackageSummaryCard(therapyPackage: TherapyPackage, isLoading: Boolean) {
    val colors = AppTheme.colors
    val total = therapyPackage.pricePerSession * therapyPackage.totalSessions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.elevated)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isLoading) {
            Text("Cargando paquete...", color = colors.muted, fontSize = 13.sp)
            return@Column
        }

        Text(
            text = therapyPackage.name,
            color = colors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        therapyPackage.doctorName?.let {
            Text(text = it, color = colors.muted, fontSize = 12.sp)
        }

        SummaryRow("Sesiones", "${therapyPackage.totalSessions}", mutedValue = true)
        SummaryRow("Precio por sesión", "S/ ${therapyPackage.pricePerSession.formatDecimal(2)}", mutedValue = true)

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Total", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("S/ ${total.formatDecimal(2)}", color = colors.navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun PaymentTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.navyTint else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) colors.navy else colors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PaymentInput(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: (@Composable () -> Unit)? = null,
    trailing: String = "",
) {
    val colors = AppTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.sand)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leading?.invoke()
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(colors.navy),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) Text(placeholder, color = colors.light, fontSize = 14.sp)
                        inner()
                    }
                },
            )
            if (trailing.isNotBlank()) {
                Text(trailing, color = colors.navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, mutedValue: Boolean = false) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = colors.muted, fontSize = 12.sp)
        Text(
            value,
            color = if (mutedValue) colors.muted else colors.text,
            fontSize = 13.sp,
            fontWeight = if (mutedValue) FontWeight.Normal else FontWeight.Bold,
        )
    }
}

private fun cardBrandLabel(cardType: CardType): String = when (cardType) {
    CardType.VISA -> "VISA"
    CardType.MASTERCARD -> "MASTERCARD"
    CardType.UNKNOWN -> ""
}

private fun visitTypeLabel(visitType: VisitType?): String = when (visitType) {
    VisitType.VIRTUAL -> "Telemedicina"
    VisitType.HOME -> "Visita a domicilio"
    VisitType.CLINIC -> "Consulta presencial"
    null -> "Consulta"
}

private fun initials(name: String): String = name
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "DR" }

private fun formatHour(hour: Int, minute: Int): String {
    val period = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "${displayHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
}

/** Formats [totalSeconds] as MM:SS — used for the Pago Pendiente countdown. */
private fun formatCountdown(totalSeconds: Int): String {
    val minutes = (totalSeconds / 60).coerceAtLeast(0)
    val seconds = (totalSeconds % 60).coerceAtLeast(0)
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
