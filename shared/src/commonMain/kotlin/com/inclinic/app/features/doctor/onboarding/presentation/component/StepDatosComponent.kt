package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value

data class StepDatosState(
    val firstName: String = "",
    val lastName: String = "",
    val cmpLicense: String = "",
    val phone: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val cmpLicenseError: String? = null,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

interface StepDatosComponent {
    val state: Value<StepDatosState>

    fun onFirstNameChanged(value: String)
    fun onLastNameChanged(value: String)
    fun onCmpLicenseChanged(value: String)
    fun onPhoneChanged(value: String)
    fun onContinueClicked()
    fun onErrorDismissed()
}
