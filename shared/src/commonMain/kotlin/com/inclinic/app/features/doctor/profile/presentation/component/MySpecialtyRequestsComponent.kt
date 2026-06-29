package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.profile.core.model.MySpecialtyRequest

interface MySpecialtyRequestsComponent {
    val state: Value<MySpecialtyRequestsState>

    fun onRetry()
    fun onRequestNew()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object RequestNew : Output
    }
}

data class MySpecialtyRequestsState(
    val isLoading: Boolean = false,
    val requests: List<MySpecialtyRequest> = emptyList(),
    val error: String? = null,
)
