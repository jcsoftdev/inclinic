package com.inclinic.app.features.doctor.packages.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage

interface PackageDetailComponent {
    val state: Value<PackageDetailState>

    fun onRetry()
    fun onCancel()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Cancelled : Output
    }
}

data class PackageDetailState(
    val isLoading: Boolean = false,
    val pkg: TherapyPackage? = null,
    val error: String? = null,
    val isCancelling: Boolean = false,
)
