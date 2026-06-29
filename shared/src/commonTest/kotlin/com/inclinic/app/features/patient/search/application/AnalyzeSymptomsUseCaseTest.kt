@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.search.application

import com.inclinic.app.core.model.AnalysisSeverity
import com.inclinic.app.core.model.RecommendedDoctor
import com.inclinic.app.core.model.SymptomAnalysis
import com.inclinic.app.core.model.SymptomSearchResult
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.SymptomAnalysisDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeSymptomDataSource : SymptomAnalysisDataSource {
    var analyzeResult: Result<SymptomSearchResult> = Result.success(
        SymptomSearchResult(
            analysis = SymptomAnalysis(
                summary = "Posible migraña tensional",
                possibleCondition = "Cefalea tensional",
                recommendedSpecialties = listOf("Neurología"),
                severity = AnalysisSeverity.MEDIUM,
            ),
            doctors = emptyList(),
        )
    )
    var lastSymptoms: String? = null
    var callCount = 0

    override suspend fun analyzeSymptoms(symptoms: String): Result<SymptomSearchResult> {
        callCount++
        lastSymptoms = symptoms
        return analyzeResult
    }
}

class AnalyzeSymptomsUseCaseTest {

    private val fake = FakeSymptomDataSource()
    private val useCase = AnalyzeSymptomsUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_forwards_symptoms_and_returns_result() = runTest {
        val result = useCase("dolor de cabeza fuerte, mareos")

        assertTrue(result.isSuccess)
        assertEquals("Posible migraña tensional", result.getOrNull()?.analysis?.summary)
        assertEquals("dolor de cabeza fuerte, mareos", fake.lastSymptoms)
        assertEquals(1, fake.callCount)
    }

    @Test
    fun success_with_doctors_in_result() = runTest {
        fake.analyzeResult = Result.success(
            SymptomSearchResult(
                analysis = SymptomAnalysis(
                    summary = "Test",
                    possibleCondition = null,
                    recommendedSpecialties = listOf("Cardiología"),
                    severity = AnalysisSeverity.LOW,
                ),
                doctors = listOf(
                    RecommendedDoctor(doctorId = "doc-1", name = "Dr. Torres", specialty = "Cardiología", rating = 4.8, reviewCount = 50, distance = "2km", matchPercentage = 95, availableToday = true, nextAvailable = null),
                ),
            )
        )

        val result = useCase("dolor en el pecho")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.doctors?.size)
        assertEquals("doc-1", result.getOrNull()?.doctors?.first()?.doctorId)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.analyzeResult = Result.failure(Exception("AI service unavailable"))

        val result = useCase("dolor")

        assertTrue(result.isFailure)
        assertEquals("AI service unavailable", result.exceptionOrNull()?.message)
    }
}
