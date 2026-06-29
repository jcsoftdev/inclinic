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
import com.inclinic.app.features.doctor.settings.infrastructure.remote.DoctorSettingsDataSource
import com.arkivanov.decompose.DefaultComponentContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

private class FakeDoctorSettingsDataSource(
    private val connectUrlResult: Result<String> = Result.success("https://mp.oauth.test/auth"),
    private val disconnectResult: Result<Unit> = Result.success(Unit),
) : DoctorSettingsDataSource {
    override suspend fun getMercadoPagoConnectUrl(): Result<String> = connectUrlResult
    override suspend fun disconnectMercadoPago(): Result<Unit> = disconnectResult
}

class DefaultDoctorSettingsComponentTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

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
        settingsDataSource: DoctorSettingsDataSource = FakeDoctorSettingsDataSource(),
    ): DefaultDoctorSettingsComponent = DefaultDoctorSettingsComponent(
        componentContext = ctx,
        logout = LogoutUseCase(tokenStorage, sessionEvents, dispatchers),
        dispatchers = dispatchers,
        settingsDataSource = settingsDataSource,
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
    fun onDeleteAccount_emits_NavigateToDeleteAccount_output() = runTest {
        val outputs = mutableListOf<DoctorSettingsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onDeleteAccount()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is DoctorSettingsComponent.Output.NavigateToDeleteAccount)
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

    // ── REQ: MercadoPago connect/disconnect ───────────────────────────────────

    @Test
    fun onConnectMercadoPago_success_sets_mercadoPagoConnectUrl_in_state() = runTest {
        val ds = FakeDoctorSettingsDataSource(
            connectUrlResult = Result.success("https://mp.oauth.test/auth?code=abc"),
        )
        val component = createComponent(settingsDataSource = ds)

        component.onConnectMercadoPago()

        assertEquals("https://mp.oauth.test/auth?code=abc", component.state.value.mercadoPagoConnectUrl)
        assertFalse(component.state.value.isMercadoPagoLoading)
        assertNull(component.state.value.mercadoPagoError)
    }

    @Test
    fun onConnectMercadoPago_mp_not_configured_sets_mercadoPagoError() = runTest {
        val ds = FakeDoctorSettingsDataSource(
            connectUrlResult = Result.failure(Exception("MP_NOT_CONFIGURED: La integración de MercadoPago no está configurada.")),
        )
        val component = createComponent(settingsDataSource = ds)

        component.onConnectMercadoPago()

        assertNull(component.state.value.mercadoPagoConnectUrl)
        assertNotNull(component.state.value.mercadoPagoError)
        val error = component.state.value.mercadoPagoError!!
        assertTrue(
            error.contains("configurad", ignoreCase = true) || error.contains("no configurada") || error.isNotBlank(),
            "Expected a meaningful error message, got: $error",
        )
    }

    @Test
    fun onMercadoPagoConnectUrlConsumed_marks_connected_true_and_clears_url() = runTest {
        val component = createComponent()
        component.onConnectMercadoPago()
        assertNotNull(component.state.value.mercadoPagoConnectUrl) // precondition

        component.onMercadoPagoConnectUrlConsumed()

        assertTrue(component.state.value.mercadoPagoConnected)
        assertNull(component.state.value.mercadoPagoConnectUrl)
    }

    @Test
    fun onDisconnectMercadoPago_success_marks_connected_false() = runTest {
        val ds = FakeDoctorSettingsDataSource(disconnectResult = Result.success(Unit))
        val component = createComponent(settingsDataSource = ds)
        // Simulate connected state
        component.onConnectMercadoPago()
        component.onMercadoPagoConnectUrlConsumed()
        assertTrue(component.state.value.mercadoPagoConnected) // precondition

        component.onDisconnectMercadoPago()

        assertFalse(component.state.value.mercadoPagoConnected)
        assertNull(component.state.value.mercadoPagoError)
    }

    @Test
    fun onDisconnectMercadoPago_failure_sets_mercadoPagoError() = runTest {
        val ds = FakeDoctorSettingsDataSource(
            disconnectResult = Result.failure(Exception("Network error")),
        )
        val component = createComponent(settingsDataSource = ds)

        component.onDisconnectMercadoPago()

        assertNotNull(component.state.value.mercadoPagoError)
        assertFalse(component.state.value.isMercadoPagoLoading)
    }
}
