package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultAdminMasMenuComponent(
    componentContext: ComponentContext,
    dispatchers: AppDispatchers,
    private val onOutput: (AdminMasMenuComponent.Output) -> Unit,
) : AdminMasMenuComponent, ComponentContext by componentContext {

    // Keep a scope even though state is static — consistent with sibling components
    // and allows future use-case calls without structural change.
    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminMasMenuState())
    override val state: Value<AdminMasMenuState> = _state

    override fun onMenuItemSelected(item: MasMenuItem) {
        val output = when (item) {
            MasMenuItem.Patients      -> AdminMasMenuComponent.Output.NavigateToPatients
            MasMenuItem.Specialties   -> AdminMasMenuComponent.Output.NavigateToSpecialties
            MasMenuItem.Reports       -> AdminMasMenuComponent.Output.NavigateToReports
            MasMenuItem.Reviews       -> AdminMasMenuComponent.Output.NavigateToReviews
            MasMenuItem.BlockedEmails -> AdminMasMenuComponent.Output.NavigateToBlockedEmails
            MasMenuItem.Subscriptions -> AdminMasMenuComponent.Output.NavigateToSubscriptions
            MasMenuItem.Profile       -> AdminMasMenuComponent.Output.NavigateToProfile
            MasMenuItem.Notifications -> AdminMasMenuComponent.Output.NavigateToNotifications
            MasMenuItem.Security      -> AdminMasMenuComponent.Output.NavigateToSecurity
        }
        onOutput(output)
    }

    override fun onSettingsClicked() {
        onOutput(AdminMasMenuComponent.Output.NavigateToConfig)
    }
}
