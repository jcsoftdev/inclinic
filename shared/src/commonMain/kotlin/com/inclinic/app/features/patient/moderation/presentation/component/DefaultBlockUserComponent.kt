package com.inclinic.app.features.patient.moderation.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.moderation.application.BlockUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultBlockUserComponent(
    componentContext: ComponentContext,
    private val targetUserId: String,
    private val targetUserName: String,
    private val blockUser: BlockUserUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (BlockUserComponent.Output) -> Unit,
) : BlockUserComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(
        BlockUserState(targetUserId = targetUserId, targetUserName = targetUserName)
    )
    override val state: Value<BlockUserState> = _state

    override fun onReasonChanged(reason: String) {
        _state.update { it.copy(reason = reason, error = null) }
    }

    override fun onConfirm() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val reason = _state.value.reason.ifBlank { null }
            blockUser(targetUserId, reason)
                .onSuccess { onOutput(BlockUserComponent.Output.Blocked) }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onCancel() { onOutput(BlockUserComponent.Output.Back) }
}
