package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecordDetail

interface MedicalRecordDetailComponent {
    val state: Value<MedicalRecordDetailState>
    fun onBack()
    sealed interface Output { data object Back : Output }
}

data class MedicalRecordDetailState(
    val record: MedicalRecordDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
