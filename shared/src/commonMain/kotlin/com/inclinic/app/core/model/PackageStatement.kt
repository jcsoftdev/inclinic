package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * Estado de cuenta de un paquete con pago progresivo.
 *
 * El descuento del paquete se erosiona con el número de abonos (1-2 pagos lo
 * conservan; del 3º en adelante se pierde por tramos), así que el [total] y el
 * [balance] pueden SUBIR si el paciente sigue fraccionando. [nextPaymentProjection]
 * anticipa ese coste para que lo vea antes de decidir.
 */
@Serializable
data class PackageStatement(
    val packageId: String,
    val packageName: String,
    val status: String,
    val totalSessions: Int,
    val paymentsCount: Int,
    /** Precio por sesión vigente tras la erosión del descuento. */
    val unitPrice: Double,
    /** Total del plan al precio vigente. */
    val total: Double,
    val amountPaid: Double,
    /** Saldo pendiente al precio vigente. */
    val balance: Double,
    val discount: Double,
    val maxDiscount: Double,
    val discountLost: Double,
    /** Sesiones íntegramente cubiertas por lo pagado (candado del médico). */
    val sessionsUnlocked: Int,
    val sessionsUsed: Int,
    val nextSession: Int,
    /** Lo que falta abonar para habilitar la siguiente sesión. 0 = ya cubierta. */
    val minimumNextPayment: Double,
    val canScheduleNext: Boolean,
    val entryPercent: Double,
    val entryAmount: Double,
    /** Pagar esto ahora congela el precio vigente y liquida el plan. */
    val payoffAmount: Double,
    /** Qué pasaría con un abono parcial más. null = el descuento ya se agotó. */
    val nextPaymentProjection: StatementProjection? = null,
    val payments: List<StatementPayment> = emptyList(),
)

@Serializable
data class StatementProjection(
    val paymentsCount: Int,
    val unitPrice: Double,
    val total: Double,
    /** Cuánto sube el total del plan si fracciona una vez más. */
    val totalIncrease: Double,
    val balanceAfterMinimum: Double,
)

@Serializable
data class StatementPayment(
    val id: String,
    val amount: Double,
    val paymentNumber: Int,
    val unitPriceAtPayment: Double,
    val totalAtPayment: Double,
    val isEntry: Boolean,
    val createdAt: Instant,
)
