package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminPendingDoctor

interface AdminPendingDoctorsComponent {
    val state: Value<AdminPendingDoctorsState>

    fun onRefresh()
    fun onDoctorClicked(doctorId: String)
    fun onBack()

    sealed interface Output {
        data class NavigateToPendingDetail(val doctorId: String) : Output
        data object Back : Output
    }
}

data class AdminPendingDoctorsState(
    val items: List<AdminPendingDoctor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
