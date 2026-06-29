package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Full-screen delete-account confirmation — design node PR3DC.
 *
 * Owns password input + destructive submit; delegates actual deletion to
 * [DefaultDeleteAccountComponent] which calls [DeleteAccountUseCase] then logout.
 */
interface DeleteAccountComponent {
    val state: Value<DeleteAccountState>

    fun onPasswordChange(value: String)
    fun onConfirm()
    fun onBack()
    fun onDismissError()

    sealed interface Output {
        data object Back : Output
        /** Account was deleted; session cleaned up via SessionEvents. */
        data object Deleted : Output
    }
}

data class DeleteAccountState(
    val password: String = "",
    val isDeleting: Boolean = false,
    val error: String? = null,
)
