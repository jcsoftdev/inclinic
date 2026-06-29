package com.inclinic.app.features.admin.twofactor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorStatus

interface AdminSecurityComponent {
    val state: Value<AdminSecurityState>

    fun onRetry()
    fun onSetupTwoFactor()
    fun onDisableTwoFactor()

    /** Called from the disable-code dialog when the user enters a code and confirms. */
    fun onDisableConfirm(code: String)
    fun onDisableDialogDismiss()
    fun onErrorDismissed()
    fun onBack()
}

data class AdminSecurityState(
    val isLoading: Boolean = true,
    val status: TwoFactorStatus? = null,
    val error: String? = null,
    /** Controls the disable-2FA dialog visibility. */
    val showDisableDialog: Boolean = false,
    val disableCode: String = "",
    val isDisabling: Boolean = false,
    val disableError: String? = null,
    val successMessage: String? = null,
)
