package com.inclinic.app.features.doctor.packages.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.application.CreatePackageUseCase
import com.inclinic.app.features.doctor.packages.core.port.NewPackageDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultCreatePackageComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    patientName: String,
    patientEmail: String,
    specialties: List<SpecialtyOption>,
    private val createPackage: CreatePackageUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (CreatePackageComponent.Output) -> Unit,
) : CreatePackageComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(
        CreatePackageState(
            patientId = patientId,
            patientName = patientName,
            patientEmail = patientEmail,
            specialties = specialties,
            selectedSpecialtyId = specialties.firstOrNull()?.id,
        ),
    )
    override val state: Value<CreatePackageState> = _state

    override fun onPackageNameChange(value: String) { _state.update { it.copy(packageName = value, nameError = null) } }
    override fun onSpecialtySelected(specialtyId: String) { _state.update { it.copy(selectedSpecialtyId = specialtyId, specialtyError = null) } }
    override fun onTotalSessionsChange(value: String) { _state.update { it.copy(totalSessions = value, sessionsError = null) } }
    override fun onRegularPriceChange(value: String) { _state.update { it.copy(regularPrice = value, regularPriceError = null) } }
    override fun onPackagePriceChange(value: String) { _state.update { it.copy(packagePrice = value, packagePriceError = null) } }
    override fun onPrepaidToggle(enabled: Boolean) { _state.update { it.copy(isPrepaid = enabled) } }
    override fun onHomeVisitToggle(enabled: Boolean) { _state.update { it.copy(isHomeVisit = enabled) } }

    override fun onSubmit() {
        val s = _state.value
        var hasError = false
        var next = s.copy(error = null)

        if (s.packageName.trim().length < 3) {
            next = next.copy(nameError = "El nombre debe tener al menos 3 caracteres"); hasError = true
        }
        if (s.selectedSpecialtyId.isNullOrBlank()) {
            next = next.copy(specialtyError = "Selecciona una especialidad"); hasError = true
        }
        val sessions = s.totalSessions.toIntOrNull()
        if (sessions == null || sessions < 2) {
            next = next.copy(sessionsError = "Mínimo 2 sesiones"); hasError = true
        }
        val regular = s.regularPrice.toDoubleOrNull()
        if (regular == null || regular < 1) {
            next = next.copy(regularPriceError = "Precio regular requerido"); hasError = true
        }
        val pkgPrice = s.packagePrice.toDoubleOrNull()
        if (pkgPrice == null || pkgPrice < 1) {
            next = next.copy(packagePriceError = "Precio paquete requerido"); hasError = true
        }

        _state.update { next }
        if (hasError) return

        val draft = NewPackageDraft(
            patientId = patientId,
            specialtyId = s.selectedSpecialtyId!!,
            packageName = s.packageName.trim(),
            totalSessions = sessions!!,
            regularPricePerSession = regular!!,
            packagePricePerSession = pkgPrice!!,
            isPrepaid = s.isPrepaid,
            prepaidDiscount = if (s.isPrepaid) s.prepaidDiscountPercent.toDouble() else null,
            isHomeVisit = s.isHomeVisit,
        )

        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            createPackage(draft)
                .onSuccess { onOutput(CreatePackageComponent.Output.PackageCreated) }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error creating package")) }
                }
        }
    }

    override fun onBack() {
        onOutput(CreatePackageComponent.Output.Back)
    }
}
