package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.auth.core.model.AuthUser

interface AdminProfileComponent {
    val state: Value<AdminProfileState>

    fun onOpenSecurity()
    fun onLogout()
    fun onRetry()
    fun onBack()
}

data class AdminProfileState(
    val isLoading: Boolean = true,
    val user: AuthUser? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false,
)
