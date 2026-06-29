@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.AnalysisSeverity
import com.inclinic.app.core.model.RecommendedDoctor
import com.inclinic.app.core.model.SymptomAnalysis
import com.inclinic.app.core.model.SymptomSearchResult
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.SymptomAnalysisDataSource
import com.inclinic.app.features.patient.search.application.AnalyzeSymptomsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testAnalysis(): SymptomAnalysis = SymptomAnalysis(
    summary = "Posible gripe",
    possibleCondition = "Influenza",
    recommendedSpecialties = listOf("Medicina General"),
    severity = AnalysisSeverity.LOW,
)

private fun testRecommendedDoctor(id: String = "doc-1"): RecommendedDoctor = RecommendedDoctor(
    doctorId = id, name = "Dr. Ana Torres", specialty = "Medicina General",
    rating = 4.5, reviewCount = 20, distance = null,
    matchPercentage = 92, availableToday = true, nextAvailable = null,
)

private fun testSearchResult(): SymptomSearchResult = SymptomSearchResult(
    analysis = testAnalysis(),
    doctors = listOf(testRecommendedDoctor()),
)

private class FakeSymptomAnalysisDataSource(
    private val result: Result<SymptomSearchResult> = Result.success(testSearchResult()),
) : SymptomAnalysisDataSource {
    var callCount: Int = 0
    var lastSymptoms: String? = null

    override suspend fun analyzeSymptoms(symptoms: String): Result<SymptomSearchResult> {
        callCount++
        lastSymptoms = symptoms
        return result
    }
}

class DefaultSymptomResultsComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        symptoms: String = "fiebre y tos",
        dataSource: FakeSymptomAnalysisDataSource = FakeSymptomAnalysisDataSource(),
        outputs: MutableList<SymptomResultsComponent.Output> = mutableListOf(),
    ): DefaultSymptomResultsComponent {
        return DefaultSymptomResultsComponent(
            componentContext = ctx,
            symptoms = symptoms,
            analyzeSymptoms = AnalyzeSymptomsUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_analysis_and_doctors() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.analysis)
        assertEquals("Posible gripe", state.analysis?.summary)
        assertEquals(1, state.doctors.size)
        assertEquals("doc-1", state.doctors.first().doctorId)
        assertNull(state.error)
    }

    @Test
    fun initial_symptoms_are_stored_in_state() = runTest {
        val component = createComponent(symptoms = "dolor de cabeza")

        assertEquals("dolor de cabeza", component.state.value.symptoms)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val ds = FakeSymptomAnalysisDataSource(result = Result.failure(Exception("API error")))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.analysis)
        assertNotNull(state.error)
    }

    @Test
    fun onRetry_triggers_reload() = runTest {
        val ds = FakeSymptomAnalysisDataSource()
        val component = createComponent(dataSource = ds)
        val initialCallCount = ds.callCount

        component.onRetry()

        assertTrue(ds.callCount > initialCallCount)
    }

    @Test
    fun onEditSymptoms_emits_EditSymptoms_output() = runTest {
        val outputs = mutableListOf<SymptomResultsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onEditSymptoms()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is SymptomResultsComponent.Output.EditSymptoms)
    }

    @Test
    fun onViewDoctorProfile_emits_NavigateToDoctorProfile() = runTest {
        val outputs = mutableListOf<SymptomResultsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onViewDoctorProfile("doc-1")

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is SymptomResultsComponent.Output.NavigateToDoctorProfile)
        assertEquals("doc-1", (output as SymptomResultsComponent.Output.NavigateToDoctorProfile).doctorId)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<SymptomResultsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is SymptomResultsComponent.Output.Back)
    }
}
