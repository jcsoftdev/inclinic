package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorStatus

interface AdminConfigComponent {
    val state: Value<AdminConfigState>

    fun onOpenSecurity()
    fun onRetry()
    fun onBack()
}

data class AdminConfigState(
    val isLoading: Boolean = true,
    val twoFactorStatus: TwoFactorStatus? = null,
    val error: String? = null,
)
