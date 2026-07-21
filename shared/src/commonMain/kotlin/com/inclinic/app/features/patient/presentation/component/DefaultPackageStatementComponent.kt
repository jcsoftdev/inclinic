package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.features.patient.therapy.application.GetPackageStatementUseCase
import com.inclinic.app.features.patient.therapy.application.PayPackageInstallmentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPackageStatementComponent(
    componentContext: ComponentContext,
    private val packageId: String,
    private val getStatement: GetPackageStatementUseCase,
    private val payInstallment: PayPackageInstallmentUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PackageStatementComponent.Output) -> Unit,
) : PackageStatementComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    // Declared before `init`: Kotlin initialises properties in declaration order,
    // so load() below would touch a still-null `_state` if this came after it.
    private val _state = MutableValue(PackageStatementState())
    override val state: Value<PackageStatementState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        load()
    }

    override fun onAmountChange(text: String) {
        // Solo dígitos y un separador decimal — es un monto en soles.
        val cleaned = text.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(amountInput = cleaned, amountError = null, submitError = null) }
    }

    override fun onSubmitPayment() {
        val amount = _state.value.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(amountError = "Ingresa un monto válido") }
            return
        }
        submit(amount)
    }

    override fun onPayoff() {
        val payoff = _state.value.statement?.payoffAmount ?: return
        if (payoff <= 0) return
        submit(payoff)
    }

    override fun onRetry() = load()

    override fun onErrorDismissed() {
        _state.update { it.copy(submitError = null, amountError = null) }
    }

    override fun onBack() = onOutput(PackageStatementComponent.Output.Back)

    private fun submit(amount: Double) {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, submitError = null, paymentApplied = false) }
        scope.launch {
            payInstallment(packageId, amount)
                .onSuccess {
                    // El abono cambió el precio del plan: recargar es la única
                    // forma de mostrar el saldo real recalculado por el backend.
                    _state.update { it.copy(isSubmitting = false, amountInput = "", paymentApplied = true) }
                    load()
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = err.message ?: err.toUserMessage("No se pudo registrar el abono"),
                        )
                    }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getStatement(packageId)
                .onSuccess { st ->
                    _state.update { it.copy(isLoading = false, statement = st) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error al cargar el estado de cuenta"))
                    }
                }
        }
    }
}
