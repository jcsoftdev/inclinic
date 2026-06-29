package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.patient_detail.application.PatientDetailWithHistory

interface PatientDetailComponent {
    val state: Value<PatientDetailState>

    fun onViewMedicalRecords()
    fun onBack()

    sealed interface Output {
        data class NavigateToMedicalRecords(val patientId: String) : Output
        data object Back : Output
    }
}

data class PatientDetailState(
    val data: PatientDetailWithHistory? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
