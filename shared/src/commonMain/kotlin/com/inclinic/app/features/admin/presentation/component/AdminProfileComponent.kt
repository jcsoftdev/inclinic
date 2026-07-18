package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.auth.core.model.AuthUser

interface AdminProfileComponent {
    val state: Value<AdminProfileState>

    fun onOpenSecurity()
    fun onLogout()
    fun onRetry()
    fun onBack()

    // ── Edit profile ─────────────────────────────────────────────────────────
    fun onEditStart()
    fun onEditCancel()
    fun onEditFirstNameChange(value: String)
    fun onEditLastNameChange(value: String)
    fun onEditPhoneChange(value: String)
    fun onEditSave()
}

data class AdminProfileState(
    val isLoading: Boolean = true,
    val user: AuthUser? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false,
    // ── Edit profile ─────────────────────────────────────────────────────────
    val isEditing: Boolean = false,
    val editFirstName: String = "",
    val editLastName: String = "",
    val editPhone: String = "",
    val isSaving: Boolean = false,
    val editError: String? = null,
)
