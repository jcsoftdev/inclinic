package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecord

interface MedicalHistoryComponent {
    val state: Value<MedicalHistoryState>

    fun onRefresh()
    fun onBack()
    fun onNavigateToClinicalProfile()

    sealed interface Output {
        data object Back : Output
        data object NavigateToClinicalProfile : Output
    }
}

data class MedicalHistoryState(
    val records: List<MedicalRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
