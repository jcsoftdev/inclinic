package com.inclinic.app.features.admin.twofactor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorSetup

interface AdminTwoFactorSetupComponent {
    val state: Value<AdminTwoFactorSetupState>

    fun onCodeChange(code: String)
    fun onActivate()
    fun onErrorDismissed()
    fun onBack()
}

data class AdminTwoFactorSetupState(
    val isLoadingSetup: Boolean = true,
    val setup: TwoFactorSetup? = null,
    val setupError: String? = null,
    val code: String = "",
    val isActivating: Boolean = false,
    val activateError: String? = null,
) {
    val canActivate: Boolean get() = code.length == 6 && !isActivating && setup != null
}
