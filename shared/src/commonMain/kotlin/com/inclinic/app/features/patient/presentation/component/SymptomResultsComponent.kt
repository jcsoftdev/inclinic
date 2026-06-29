package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.RecommendedDoctor
import com.inclinic.app.core.model.SymptomAnalysis

interface SymptomResultsComponent {
    val state: Value<SymptomResultsState>
    fun onEditSymptoms()
    fun onViewDoctorProfile(doctorId: String)
    fun onBack()
    fun onRetry()
    sealed interface Output {
        data object Back : Output
        data class NavigateToDoctorProfile(val doctorId: String) : Output
        data object EditSymptoms : Output
    }
}

data class SymptomResultsState(
    val symptoms: String = "",
    val analysis: SymptomAnalysis? = null,
    val doctors: List<RecommendedDoctor> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
