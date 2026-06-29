package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.PersonalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultStepDatosComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val onContinue: (PersonalData) -> Unit,
) : StepDatosComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(StepDatosState())
    override val state: Value<StepDatosState> = _state

    override fun onFirstNameChanged(value: String) {
        _state.update { it.copy(firstName = value, firstNameError = null) }
    }

    override fun onLastNameChanged(value: String) {
        _state.update { it.copy(lastName = value, lastNameError = null) }
    }

    override fun onCmpLicenseChanged(value: String) {
        _state.update { it.copy(cmpLicense = value, cmpLicenseError = null) }
    }

    override fun onPhoneChanged(value: String) {
        _state.update { it.copy(phone = value, phoneError = null) }
    }

    override fun onContinueClicked() {
        val s = _state.value
        val firstNameError = if (s.firstName.isBlank()) "Campo requerido" else null
        val lastNameError = if (s.lastName.isBlank()) "Campo requerido" else null
        val cmpLicenseError = if (s.cmpLicense.isBlank()) "Campo requerido" else null
        val phoneError = if (s.phone.isBlank()) "Campo requerido" else null

        if (firstNameError != null || lastNameError != null || cmpLicenseError != null || phoneError != null) {
            _state.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    cmpLicenseError = cmpLicenseError,
                    phoneError = phoneError,
                )
            }
            return
        }

        onContinue(
            PersonalData(
                firstName = s.firstName.trim(),
                lastName = s.lastName.trim(),
                cmpLicense = s.cmpLicense.trim(),
                phone = s.phone.trim(),
            )
        )
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
