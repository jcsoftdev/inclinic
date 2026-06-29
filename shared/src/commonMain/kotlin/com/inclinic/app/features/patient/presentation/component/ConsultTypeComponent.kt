package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Doctor

enum class ConsultType { PRESENCIAL, TELEMEDICINE, HOME_VISIT }

interface ConsultTypeComponent {
    val state: Value<ConsultTypeState>

    fun onTypeSelected(type: ConsultType)
    fun onContinue()
    fun onBack()

    sealed interface Output {
        data class NavigateToAvailability(val doctorId: String, val consultType: String) : Output
        data object Back : Output
    }
}

data class ConsultTypeState(
    val doctor: Doctor? = null,
    val selectedType: ConsultType = ConsultType.PRESENCIAL,
    val isLoading: Boolean = false,
    val error: String? = null,
)
