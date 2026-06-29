@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testProfile() = PatientProfile(
    id = "pat-1",
    name = "María López",
    email = "maria@test.com",
    phone = "999888777",
    dateOfBirth = "1990-05-15",
    photoUrl = null,
)

private class FakeSettingsPatientDataSource(
    private val profileResult: Result<PatientProfile> = Result.success(testProfile()),
) : PatientDataSource {
    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> = profileResult
    override suspend fun updatePatientProfile(patientId: String, name: String, phone: String?, dateOfBirth: String?) =
        Result.failure<PatientProfile>(UnsupportedOperationException())
    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> =
        Result.success(MedicalProfile.empty())
    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> =
        Result.success(profile)
    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> =
        Result.success(PatientDashboard(upcomingCount = 0, recentDoctors = emptyList()))
    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

class DefaultSettingsComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: PatientDataSource = FakeSettingsPatientDataSource(),
        outputs: MutableList<SettingsComponent.Output> = mutableListOf(),
    ): DefaultSettingsComponent = DefaultSettingsComponent(
        componentContext = ctx,
        patientId = "pat-1",
        getProfile = GetPatientProfileUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun load_success_sets_email_and_emailVerified_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals("maria@test.com", state.email)
        assertTrue(state.emailVerified)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeSettingsPatientDataSource(
            profileResult = Result.failure(Exception("Load error")),
        )
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals("Load error", state.error)
    }

    @Test
    fun onPushToggle_true_updates_pushEnabled() = runTest {
        val component = createComponent()
        component.onPushToggle(true)
        assertTrue(component.state.value.pushEnabled)
    }

    @Test
    fun onPushToggle_false_disables_push() = runTest {
        val component = createComponent()
        component.onPushToggle(false)
        assertFalse(component.state.value.pushEnabled)
    }

    @Test
    fun onAnalyticsToggle_true_enables_analytics() = runTest {
        val component = createComponent()
        component.onAnalyticsToggle(true)
        assertTrue(component.state.value.analyticsEnabled)
    }

    @Test
    fun onAnalyticsToggle_false_disables_analytics() = runTest {
        val component = createComponent()
        component.onAnalyticsToggle(true)
        component.onAnalyticsToggle(false)
        assertFalse(component.state.value.analyticsEnabled)
    }

    @Test
    fun onChangePassword_emits_NavigateToChangePassword_output() = runTest {
        val outputs = mutableListOf<SettingsComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onChangePassword()
        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is SettingsComponent.Output.NavigateToChangePassword)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<SettingsComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onBack()
        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is SettingsComponent.Output.Back)
    }

    @Test
    fun onDeleteAccount_emits_NavigateToDeleteAccount_output() = runTest {
        val outputs = mutableListOf<SettingsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onDeleteAccount()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is SettingsComponent.Output.NavigateToDeleteAccount)
    }
}
