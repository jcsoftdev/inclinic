package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.model.PackageStatement
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.patient.presentation.component.PackageStatementComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Estado de cuenta del pago progresivo.
 *
 * El cuadro central hace visible la regla del modelo: el saldo pendiente SUBE
 * si el paciente sigue fraccionando, y liquidar ahora congela el precio. La
 * proyección del siguiente pago se muestra ANTES de que decida abonar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageStatementScreen(
    component: PackageStatementComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val typography = AppTheme.typography
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
                    text = "Estado de cuenta",
                    style = typography.body.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
        )

        when {
            state.isLoading && state.statement == null ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }

            state.error != null -> Column(
                Modifier.fillMaxWidth().padding(dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            ) {
                ErrorBanner(message = state.error!!, modifier = Modifier.fillMaxWidth())
                AppButton(
                    text = "Reintentar",
                    onClick = component::onRetry,
                    variant = AppButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            state.statement != null -> StatementContent(
                statement = state.statement!!,
                component = component,
                amountInput = state.amountInput,
                amountError = state.amountError,
                submitError = state.submitError,
                isSubmitting = state.isSubmitting,
                paymentApplied = state.paymentApplied,
            )
        }
    }
}

@Composable
private fun StatementContent(
    statement: PackageStatement,
    component: PackageStatementComponent,
    amountInput: String,
    amountError: String?,
    submitError: String?,
    isSubmitting: Boolean,
    paymentApplied: Boolean,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimens.spacingMd),
    ) {
        // ── Resumen del plan ──────────────────────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(dimens.spacingMd),
        ) {
            Text(statement.packageName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text)
            StatRow("Pagos realizados", "${statement.paymentsCount}")
            StatRow("Precio actual por sesión", "S/ ${statement.unitPrice.formatDecimal(2)}")
            StatRow("Total del plan", "S/ ${statement.total.formatDecimal(2)}")
            StatRow("Pagado", "S/ ${statement.amountPaid.formatDecimal(2)}")
            Divider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("SALDO PENDIENTE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Text(
                    "S/ ${statement.balance.formatDecimal(2)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (statement.discountLost > 0) colors.red else colors.navy,
                )
            }
            if (statement.discountLost > 0) {
                Text(
                    "Has perdido S/ ${statement.discountLost.formatDecimal(2)} de descuento por fraccionar",
                    fontSize = 12.sp,
                    color = colors.red,
                )
            }
            StatRow(
                "Sesiones desbloqueadas",
                "${statement.sessionsUnlocked} de ${statement.totalSessions} (usadas: ${statement.sessionsUsed})",
            )
        }

        // ── Candado: qué falta para la siguiente sesión ───────────────────────
        if (statement.minimumNextPayment > 0) {
            InfoBanner(
                title = "Abono pendiente para tu próxima sesión",
                description = "Para habilitar la sesión ${statement.nextSession} debes abonar al menos " +
                    "S/ ${statement.minimumNextPayment.formatDecimal(2)}",
                tone = InfoBannerTone.Warning,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // ── Proyección: el coste de seguir fraccionando ───────────────────────
        statement.nextPaymentProjection?.let { proj ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.redBg)
                    .padding(dimens.spacing12),
            ) {
                Text(
                    "⚠️ Si haces otro pago parcial",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.red,
                )
                Text(
                    "El precio sube a S/ ${proj.unitPrice.formatDecimal(2)}/sesión y el total del plan " +
                        "a S/ ${proj.total.formatDecimal(2)} (+S/ ${proj.totalIncrease.formatDecimal(2)})",
                    fontSize = 12.sp,
                    color = colors.text,
                )
            }
        }

        // ── Liquidar: congela el precio vigente ───────────────────────────────
        if (statement.payoffAmount > 0) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.navyTint)
                    .padding(dimens.spacing12),
            ) {
                Text(
                    "💡 Paga el saldo completo ahora y conservas S/ ${statement.unitPrice.formatDecimal(2)} por sesión",
                    fontSize = 13.sp,
                    color = colors.text,
                )
                AppButton(
                    text = "Pagar saldo completo · S/ ${statement.payoffAmount.formatDecimal(2)}",
                    onClick = component::onPayoff,
                    loading = isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // ── Abono parcial ─────────────────────────────────────────────────────
        if (statement.balance > 0) {
            Text("Abonar otro monto", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
            AppTextField(
                value = amountInput,
                onValueChange = component::onAmountChange,
                label = "Monto en soles",
                placeholder = "Ej. ${statement.unitPrice.formatDecimal(0)}",
                error = amountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Abonar",
                onClick = component::onSubmitPayment,
                variant = AppButtonVariant.Outline,
                loading = isSubmitting,
                enabled = amountInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (submitError != null) {
            ErrorBanner(message = submitError, modifier = Modifier.fillMaxWidth())
        }
        if (paymentApplied) {
            InfoBanner(
                title = "Abono aplicado",
                description = "Tu saldo se actualizó con el precio vigente",
                tone = InfoBannerTone.Success,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // ── Historial de abonos ───────────────────────────────────────────────
        if (statement.payments.isNotEmpty()) {
            Text("Historial de pagos", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            ) {
                statement.payments.forEachIndexed { index, p ->
                    if (index > 0) Divider()
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(dimens.spacing12),
                    ) {
                        Column {
                            Text(
                                if (p.isEntry) "Entrada (pago #${p.paymentNumber})" else "Pago #${p.paymentNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text,
                            )
                            val date = p.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            Text(
                                "$date · a S/ ${p.unitPriceAtPayment.formatDecimal(2)}/sesión",
                                fontSize = 11.sp,
                                color = colors.muted,
                            )
                        }
                        Text(
                            "S/ ${p.amount.formatDecimal(2)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(dimens.spacingMd))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val colors = AppTheme.colors
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label, fontSize = 13.sp, color = colors.muted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.text)
    }
}

@Composable
private fun Divider() {
    val colors = AppTheme.colors
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
}
