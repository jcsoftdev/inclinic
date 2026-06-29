package com.inclinic.app.features.doctor.therapy_offers.presentation.component

import com.arkivanov.decompose.value.Value

data class SpecialtyOption(val id: String, val name: String)

interface CreateTherapyOfferComponent {
    val state: Value<CreateTherapyOfferState>

    fun onTitleChange(v: String)
    fun onSpecialtySelected(id: String)
    fun onTotalSessionsChange(v: String)
    fun onPricePerSessionChange(v: String)
    fun onMinPriceChange(v: String)
    fun onDescriptionChange(v: String)
    fun onActiveToggle(v: Boolean)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object OfferCreated : Output
        data object Back : Output
    }
}

data class CreateTherapyOfferState(
    val title: String = "",
    val specialties: List<SpecialtyOption> = emptyList(),
    val selectedSpecialtyId: String = "",
    val totalSessions: String = "",
    val pricePerSession: String = "",
    val minPricePerSession: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    // field errors
    val titleError: String? = null,
    val specialtyError: String? = null,
    val sessionsError: String? = null,
    val priceError: String? = null,
)
