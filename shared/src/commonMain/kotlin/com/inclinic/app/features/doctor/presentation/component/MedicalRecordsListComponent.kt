package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecord

interface MedicalRecordsListComponent {
    val state: Value<MedicalRecordsListState>

    fun onCreateRecord()
    fun onRecordTap(recordId: String)
    fun onBack()

    sealed interface Output {
        data class NavigateToCreateRecord(val patientId: String, val appointmentId: String? = null) : Output
        data class NavigateToEditRecord(val recordId: String) : Output
        data object Back : Output
    }
}

data class MedicalRecordsListState(
    val records: List<MedicalRecord> = emptyList(),
    val isLoading: Boolean = false,
    val expandedRecordId: String? = null,
    val error: String? = null,
)
