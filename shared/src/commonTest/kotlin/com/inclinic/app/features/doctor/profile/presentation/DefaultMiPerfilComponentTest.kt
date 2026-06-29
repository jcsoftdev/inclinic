package com.inclinic.app.features.doctor.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetDoctorProfileUseCase
import com.inclinic.app.features.doctor.profile.application.UpdateDoctorProfileUseCase
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultMiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeMiPerfilTokenStorage : TokenStorage {
    var cleared = false
    private var tokens: AuthTokens? = AuthTokens("access", "refresh")
    override suspend fun save(tokens: AuthTokens) { this.tokens = tokens }
    override suspend fun load(): AuthTokens? = tokens
    override suspend fun clear() { cleared = true; tokens = null }
    override suspend fun saveUser(user: AuthUser) {}
    override suspend fun loadUser(): AuthUser? = null
}

class DefaultMiPerfilComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val tokenStorage = FakeMiPerfilTokenStorage()
    private val sessionEvents = SessionEvents()

    private fun makeComponent(
        onOutput: (MiPerfilComponent.Output) -> Unit = {},
    ): DefaultMiPerfilComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultMiPerfilComponent(
            componentContext = ctx,
            getProfile = GetDoctorProfileUseCase(fakeRepo, dispatchers),
            updateProfile = UpdateDoctorProfileUseCase(fakeRepo, dispatchers),
            logout = LogoutUseCase(tokenStorage, sessionEvents, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    @Test
    fun initial_load_populates_profile_on_success() = runTest {
        fakeRepo.getProfileResult = Result.success(FakeDoctorProfileRepository.defaultProfile)

        val component = makeComponent()

        assertNotNull(component.state.value.profile)
        assertEquals("doc-1", component.state.value.profile?.id)
        assertNull(component.state.value.error)
    }

    @Test
    fun initial_load_sets_error_on_failure() = runTest {
        fakeRepo.getProfileResult = Result.failure(RuntimeException("Network error"))

        val component = makeComponent()

        assertNull(component.state.value.profile)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onRetry_calls_getProfile_again() = runTest {
        fakeRepo.getProfileResult = Result.failure(RuntimeException("timeout"))
        val component = makeComponent()

        fakeRepo.getProfileResult = Result.success(FakeDoctorProfileRepository.defaultProfile)
        component.onRetry()

        assertEquals(2, fakeRepo.getProfileCallCount) // first from init, second from retry
        assertNotNull(component.state.value.profile)
    }

    @Test
    fun onNavigateEditSpecialties_emits_EditSpecialties_output() = runTest {
        var output: MiPerfilComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onNavigateEditSpecialties()

        assertTrue(output is MiPerfilComponent.Output.EditSpecialties)
    }

    @Test
    fun onNavigateIncome_emits_Income_output() = runTest {
        var output: MiPerfilComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onNavigateIncome()

        assertTrue(output is MiPerfilComponent.Output.Income)
    }

    @Test
    fun onNavigateEditHorarios_emits_EditHorarios_output() = runTest {
        var output: MiPerfilComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onNavigateEditHorarios()

        assertTrue(output is MiPerfilComponent.Output.EditHorarios)
    }

    @Test
    fun onNavigateTherapyOffers_emits_TherapyOffers_output() = runTest {
        var output: MiPerfilComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onNavigateTherapyOffers()

        assertTrue(output is MiPerfilComponent.Output.TherapyOffers)
    }

    @Test
    fun onLogout_emits_Logout_output() = runTest {
        var output: MiPerfilComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onLogout()

        assertTrue(output is MiPerfilComponent.Output.Logout)
    }
}
