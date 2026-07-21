package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.PackageStatement

/**
 * Estado de cuenta de un paquete con pago progresivo.
 *
 * Muestra el saldo vigente y —clave del modelo— cuánto sube el plan si el
 * paciente sigue fraccionando, para que prefiera liquidar cuanto antes.
 */
interface PackageStatementComponent {
    val state: Value<PackageStatementState>

    fun onAmountChange(text: String)
    fun onSubmitPayment()

    /** Abona el saldo completo, congelando el precio vigente. */
    fun onPayoff()
    fun onRetry()
    fun onErrorDismissed()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class PackageStatementState(
    val isLoading: Boolean = true,
    val statement: PackageStatement? = null,
    val error: String? = null,
    /** Monto que el paciente escribe para abonar. */
    val amountInput: String = "",
    val amountError: String? = null,
    val isSubmitting: Boolean = false,
    /** Mensaje del backend cuando un abono se rechaza (entrada/abono mínimo). */
    val submitError: String? = null,
    /** Se muestra brevemente tras aplicar un abono con éxito. */
    val paymentApplied: Boolean = false,
)
