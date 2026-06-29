package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail

interface AdminDoctorDetailComponent {
    val state: Value<AdminDoctorDetailState>

    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminDoctorDetailState(
    val detail: AdminDoctorDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
