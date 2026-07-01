package com.inclinic.app.features.patient.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.fakes.FakePatientDataSource
import com.inclinic.app.features.patient.presentation.component.ChangePasswordComponent
import com.inclinic.app.features.patient.presentation.component.DefaultChangePasswordComponent
import com.inclinic.app.features.patient.profile.application.ChangePasswordUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultChangePasswordComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeDataSource = FakePatientDataSource()
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onOutput: (ChangePasswordComponent.Output) -> Unit = {},
    ): DefaultChangePasswordComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultChangePasswordComponent(
            componentContext = ctx,
            changePassword = ChangePasswordUseCase(fakeDataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_is_empty_and_clean() {
        val component = makeComponent()
        val state = component.state.value
        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmNewPassword)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.success)
    }

    // ── Field updates ─────────────────────────────────────────────────────────

    @Test
    fun onCurrentPasswordChange_updates_state() {
        val component = makeComponent()
        component.onCurrentPasswordChange("secret")
        assertEquals("secret", component.state.value.currentPassword)
    }

    @Test
    fun onNewPasswordChange_updates_state() {
        val component = makeComponent()
        component.onNewPasswordChange("newpass1")
        assertEquals("newpass1", component.state.value.newPassword)
    }

    @Test
    fun onConfirmNewPasswordChange_updates_state() {
        val component = makeComponent()
        component.onConfirmNewPasswordChange("newpass1")
        assertEquals("newpass1", component.state.value.confirmNewPassword)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun onSubmit_with_mismatched_passwords_sets_error_without_network_call() = runTest {
        val component = makeComponent()
        component.onCurrentPasswordChange("current123")
        component.onNewPasswordChange("newpass1")
        component.onConfirmNewPasswordChange("different")

        component.onSubmit()

        assertNotNull(component.state.value.error)
        assertFalse(component.state.value.success)
        assertEquals(0, fakeDataSource.changePasswordCallCount)
    }

    @Test
    fun onSubmit_with_short_new_password_sets_error_without_network_call() = runTest {
        val component = makeComponent()
        component.onCurrentPasswordChange("current123")
        component.onNewPasswordChange("abc")
        component.onConfirmNewPasswordChange("abc")

        component.onSubmit()

        assertNotNull(component.state.value.error)
        assertFalse(component.state.value.success)
        assertEquals(0, fakeDataSource.changePasswordCallCount)
    }

    @Test
    fun onSubmit_with_empty_current_password_sets_error_without_network_call() = runTest {
        val component = makeComponent()
        component.onCurrentPasswordChange("")
        component.onNewPasswordChange("newpass1")
        component.onConfirmNewPasswordChange("newpass1")

        component.onSubmit()

        assertNotNull(component.state.value.error)
        assertEquals(0, fakeDataSource.changePasswordCallCount)
    }

    // ── Success path ──────────────────────────────────────────────────────────

    @Test
    fun onSubmit_success_path_sets_success_state() = runTest {
        fakeDataSource.changePasswordResult = Result.success(Unit)
        val component = makeComponent()
        component.onCurrentPasswordChange("current123")
        component.onNewPasswordChange("newpass123")
        component.onConfirmNewPasswordChange("newpass123")

        component.onSubmit()

        assertTrue(component.state.value.success)
        assertNull(component.state.value.error)
        assertFalse(component.state.value.isLoading)
        assertEquals(1, fakeDataSource.changePasswordCallCount)
    }

    // ── INVALID_CREDENTIALS ───────────────────────────────────────────────────

    @Test
    fun onSubmit_INVALID_CREDENTIALS_maps_to_friendly_message() = runTest {
        fakeDataSource.changePasswordResult = Result.failure(RuntimeException("INVALID_CREDENTIALS"))
        val component = makeComponent()
        component.onCurrentPasswordChange("wrong")
        component.onNewPasswordChange("newpass123")
        component.onConfirmNewPasswordChange("newpass123")

        component.onSubmit()

        val error = component.state.value.error
        assertNotNull(error)
        assertTrue(error.contains("incorrecta", ignoreCase = true))
        assertFalse(component.state.value.success)
    }

    // ── Generic error ─────────────────────────────────────────────────────────

    @Test
    fun onSubmit_generic_error_sets_error_state() = runTest {
        fakeDataSource.changePasswordResult = Result.failure(RuntimeException("Network timeout"))
        val component = makeComponent()
        component.onCurrentPasswordChange("current123")
        component.onNewPasswordChange("newpass123")
        component.onConfirmNewPasswordChange("newpass123")

        component.onSubmit()

        assertNotNull(component.state.value.error)
        assertFalse(component.state.value.success)
    }

    // ── Back output ───────────────────────────────────────────────────────────

    @Test
    fun onBack_emits_Back_output() {
        var output: ChangePasswordComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onBack()

        assertTrue(output is ChangePasswordComponent.Output.Back)
    }
}
