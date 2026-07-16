@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private fun profile(name: String) = PatientProfile(
    id = "pat-1",
    name = name,
    email = "paciente@test.com",
    phone = "900209147",
    dateOfBirth = null,
    photoUrl = null,
)

/** Returns whatever `current` holds at call time, so the test can mutate it between loads. */
private class SwitchingProfileDataSource : PatientDataSource {
    var current: Result<PatientProfile> = Result.success(profile("Juan Paciente"))
    var getProfileCallCount = 0

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> {
        getProfileCallCount++
        return current
    }

    override suspend fun updatePatientProfile(
        patientId: String,
        name: String,
        phone: String?,
        dateOfBirth: String?,
    ): Result<PatientProfile> = current

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> =
        Result.success(MedicalProfile.empty())

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> =
        Result.success(profile)

    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> =
        Result.success(PatientDashboard(upcomingCount = 0, recentDoctors = emptyList()))

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> =
        Result.success(Unit)

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        Result.success(Unit)
}

private class FakeOverviewTokenStorage : TokenStorage {
    private var tokens: AuthTokens? = AuthTokens("access", "refresh")
    override suspend fun save(tokens: AuthTokens) { this.tokens = tokens }
    override suspend fun load(): AuthTokens? = tokens
    override suspend fun clear() { tokens = null }
    override suspend fun saveUser(user: AuthUser) {}
    override suspend fun loadUser(): AuthUser? = null
}

class DefaultProfileOverviewComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry()

    private fun makeComponent(ds: SwitchingProfileDataSource): DefaultProfileOverviewComponent {
        // Mirror Decompose: the child subscribes to its lifecycle BEFORE being resumed,
        // so doOnResume fires the initial load on the resume below (not on construction).
        val ctx = DefaultComponentContext(lifecycle)
        val component = DefaultProfileOverviewComponent(
            componentContext = ctx,
            patientId = "pat-1",
            getProfile = GetPatientProfileUseCase(ds, dispatchers),
            getMedicalProfile = GetMedicalProfileUseCase(ds, dispatchers),
            logout = LogoutUseCase(FakeOverviewTokenStorage(), SessionEvents(), dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )
        lifecycle.resume()
        return component
    }

    @Test
    fun initial_resume_loads_profile() = runTest {
        val ds = SwitchingProfileDataSource()

        val component = makeComponent(ds)

        assertNotNull(component.state.value.profile)
        assertEquals("Juan Paciente", component.state.value.profile?.name)
    }

    @Test
    fun reloads_profile_on_resume_so_ui_reflects_edits() = runTest {
        val ds = SwitchingProfileDataSource()
        val component = makeComponent(ds)
        assertEquals("Juan Paciente", component.state.value.profile?.name)

        // Simulate going to EditProfile (pause) and returning after a save (resume).
        ds.current = Result.success(profile("Juan Carlos Paciente"))
        lifecycle.pause()
        lifecycle.resume()

        assertEquals(2, ds.getProfileCallCount)
        assertEquals("Juan Carlos Paciente", component.state.value.profile?.name)
    }
}
