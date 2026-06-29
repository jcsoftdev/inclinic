@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.settings.presentation.component

import app.cash.turbine.test
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.arkivanov.decompose.DefaultComponentContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeTokenStorage : TokenStorage {
    var cleared = false
    private var tokens: AuthTokens? = AuthTokens("access", "refresh")
    override suspend fun save(tokens: AuthTokens) { this.tokens = tokens }
    override suspend fun load(): AuthTokens? = tokens
    override suspend fun clear() { cleared = true; tokens = null }
    override suspend fun saveUser(user: AuthUser) {}
    override suspend fun loadUser(): AuthUser? = null
}

class DefaultDoctorSettingsComponentTest {

    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()

    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val lifecycle = LifecycleRegistry()
    private val ctx: ComponentContext = DefaultComponentContext(lifecycle)

    private val tokenStorage = FakeTokenStorage()
    private val sessionEvents = SessionEvents()

    private fun createComponent(
        outputs: MutableList<DoctorSettingsComponent.Output> = mutableListOf(),
    ): DefaultDoctorSettingsComponent = DefaultDoctorSettingsComponent(
        componentContext = ctx,
        logout = LogoutUseCase(tokenStorage, sessionEvents, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun initial_state_has_default_toggle_values() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertTrue(state.newAppointmentsEnabled)
        assertTrue(state.chatMessagesEnabled)
        assertFalse(state.appointmentRemindersEnabled)
        assertFalse(state.twoFactorEnabled)
    }

    @Test
    fun onToggleNewAppointments_updates_state() = runTest {
        val component = createComponent()

        component.onToggleNewAppointments(false)

        assertFalse(component.state.value.newAppointmentsEnabled)
    }

    @Test
    fun onToggleChatMessages_updates_state() = runTest {
        val component = createComponent()

        component.onToggleChatMessages(false)

        assertFalse(component.state.value.chatMessagesEnabled)
    }

    @Test
    fun onToggleAppointmentReminders_updates_state() = runTest {
        val component = createComponent()

        component.onToggleAppointmentReminders(true)

        assertTrue(component.state.value.appointmentRemindersEnabled)
    }

    @Test
    fun onToggleTwoFactor_updates_state() = runTest {
        val component = createComponent()

        component.onToggleTwoFactor(true)

        assertTrue(component.state.value.twoFactorEnabled)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<DoctorSettingsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is DoctorSettingsComponent.Output.Back)
    }

    @Test
    fun onLogOut_clears_tokens_emits_session_expired_and_LoggedOut_output() = runTest {
        val outputs = mutableListOf<DoctorSettingsComponent.Output>()
        val component = createComponent(outputs = outputs)

        sessionEvents.expired.test {
            component.onLogOut()
            testScheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(tokenStorage.cleared)
        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is DoctorSettingsComponent.Output.LoggedOut)
    }
}
