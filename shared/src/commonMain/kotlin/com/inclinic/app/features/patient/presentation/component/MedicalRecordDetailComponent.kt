package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecordDetail

interface MedicalRecordDetailComponent {
    val state: Value<MedicalRecordDetailState>
    fun onBack()
    fun onNavigateToMembership()
    sealed interface Output {
        data object Back : Output
        data object NavigateToMembership : Output
    }
}

data class MedicalRecordDetailState(
    val record: MedicalRecordDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
