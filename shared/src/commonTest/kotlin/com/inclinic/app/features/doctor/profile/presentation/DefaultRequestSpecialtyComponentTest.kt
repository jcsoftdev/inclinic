package com.inclinic.app.features.doctor.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.application.RequestSpecialtyUseCase
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultRequestSpecialtyComponent
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultRequestSpecialtyComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onOutput: (RequestSpecialtyComponent.Output) -> Unit = {},
    ): DefaultRequestSpecialtyComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultRequestSpecialtyComponent(
            componentContext = ctx,
            requestSpecialty = RequestSpecialtyUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    @Test
    fun initial_state_is_blank() {
        val component = makeComponent()
        val s = component.state.value
        assertEquals("", s.specialtyName)
        assertEquals("", s.comment)
        assertTrue(s.documentUrls.isEmpty())
        assertNull(s.error)
    }

    @Test
    fun onSpecialtyNameChange_updates_state() {
        val component = makeComponent()
        component.onSpecialtyNameChange("Neurología")
        assertEquals("Neurología", component.state.value.specialtyName)
    }

    @Test
    fun onCommentChange_updates_state() {
        val component = makeComponent()
        component.onCommentChange("Tengo experiencia.")
        assertEquals("Tengo experiencia.", component.state.value.comment)
    }

    @Test
    fun onAddDocumentUrl_appends_url() {
        val component = makeComponent()
        component.onAddDocumentUrl("https://cdn.inclinic.com/doc-a")
        assertEquals(1, component.state.value.documentUrls.size)
    }

    @Test
    fun onRemoveDocumentUrl_removes_url() {
        val component = makeComponent()
        component.onAddDocumentUrl("https://cdn.inclinic.com/doc-a")
        component.onRemoveDocumentUrl("https://cdn.inclinic.com/doc-a")
        assertEquals(0, component.state.value.documentUrls.size)
    }

    @Test
    fun onSubmit_sets_error_when_specialtyName_is_blank() = runTest {
        val component = makeComponent()

        component.onSubmit()

        assertNotNull(component.state.value.error)
        assertEquals(0, fakeRepo.requestSpecialtyCallCount)
    }

    @Test
    fun onSubmit_calls_repository_with_trimmed_values() = runTest {
        val component = makeComponent()
        component.onSpecialtyNameChange("  Neurología  ")
        component.onCommentChange("  Experiencia.  ")

        component.onSubmit()

        assertEquals(1, fakeRepo.requestSpecialtyCallCount)
        assertEquals("Neurología", fakeRepo.lastSpecialtyRequest?.specialtyName)
        assertEquals("Experiencia.", fakeRepo.lastSpecialtyRequest?.comment)
    }

    @Test
    fun onSubmit_emits_Submitted_output_on_success() = runTest {
        var output: RequestSpecialtyComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })
        component.onSpecialtyNameChange("Neurología")

        component.onSubmit()

        assertTrue(output is RequestSpecialtyComponent.Output.Submitted)
    }

    @Test
    fun onSubmit_sets_error_on_failure() = runTest {
        fakeRepo.requestSpecialtyResult = Result.failure(RuntimeException("Server error"))
        val component = makeComponent()
        component.onSpecialtyNameChange("Neurología")

        component.onSubmit()

        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() {
        var output: RequestSpecialtyComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onBack()

        assertTrue(output is RequestSpecialtyComponent.Output.Back)
    }
}
