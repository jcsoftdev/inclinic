package com.inclinic.app.features.doctor.packages.presentation.component

import com.arkivanov.decompose.value.Value

interface CreatePackageComponent {
    val state: Value<CreatePackageState>

    fun onPackageNameChange(value: String)
    fun onSpecialtySelected(specialtyId: String)
    fun onTotalSessionsChange(value: String)
    fun onRegularPriceChange(value: String)
    fun onPackagePriceChange(value: String)
    fun onPrepaidToggle(enabled: Boolean)
    fun onHomeVisitToggle(enabled: Boolean)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object PackageCreated : Output
        data object Back : Output
    }
}

/** A specialty the doctor can sell this package under. */
data class SpecialtyOption(val id: String, val name: String)

data class CreatePackageState(
    val patientId: String = "",
    val patientName: String = "",
    val patientEmail: String = "",
    val specialties: List<SpecialtyOption> = emptyList(),
    val selectedSpecialtyId: String? = null,
    val packageName: String = "",
    val totalSessions: String = "",
    val regularPrice: String = "",
    val packagePrice: String = "",
    val isPrepaid: Boolean = false,
    val isHomeVisit: Boolean = false,
    val prepaidDiscountPercent: Int = 15,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val specialtyError: String? = null,
    val sessionsError: String? = null,
    val regularPriceError: String? = null,
    val packagePriceError: String? = null,
) {
    /** Total package price before prepaid discount, in soles. */
    val totalBeforeDiscount: Double
        get() = (packagePrice.toDoubleOrNull() ?: 0.0) * (totalSessions.toIntOrNull() ?: 0)

    /** Total the patient pays, applying prepaid discount when prepaid. */
    val totalWithDiscount: Double
        get() = if (isPrepaid) totalBeforeDiscount * (1 - prepaidDiscountPercent / 100.0) else totalBeforeDiscount

    /** Amount saved with the prepaid discount, in soles. */
    val discountSavings: Double
        get() = totalBeforeDiscount - totalWithDiscount
}
