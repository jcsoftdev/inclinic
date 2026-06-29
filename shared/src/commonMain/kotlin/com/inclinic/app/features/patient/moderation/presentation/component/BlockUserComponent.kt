package com.inclinic.app.features.patient.moderation.presentation.component

import com.arkivanov.decompose.value.Value

interface BlockUserComponent {
    val state: Value<BlockUserState>

    fun onReasonChanged(reason: String)
    fun onConfirm()
    fun onCancel()

    sealed interface Output {
        data object Blocked : Output
        data object Back : Output
    }
}

data class BlockUserState(
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
