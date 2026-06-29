package com.inclinic.app.features.splash.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.GetStoredTokensUseCase
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.core.port.TokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultSplashComponent(
    componentContext: ComponentContext,
    private val getStoredTokens: GetStoredTokensUseCase,
    private val tokenStorage: TokenStorage,
    private val dispatchers: AppDispatchers,
    private val onOutput: (SplashComponent.Output) -> Unit,
) : SplashComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        lifecycle.doOnStart { checkSession() }
    }

    private fun checkSession() {
        scope.launch {
            val tokens = getStoredTokens()
            if (tokens == null) {
                onOutput(SplashComponent.Output.NavigateToAuth)
                return@launch
            }
            val user = tokenStorage.loadUser()
            if (user == null) {
                onOutput(SplashComponent.Output.NavigateToAuth)
                return@launch
            }
            when (user.role) {
                UserRole.DOCTOR -> onOutput(SplashComponent.Output.NavigateToDoctor(user.doctorId ?: user.id))
                UserRole.SUPER_ADMIN -> onOutput(SplashComponent.Output.NavigateToAdmin(user.id))
                else -> onOutput(SplashComponent.Output.NavigateToPatient(user.patientId ?: user.id))
            }
        }
    }
}
