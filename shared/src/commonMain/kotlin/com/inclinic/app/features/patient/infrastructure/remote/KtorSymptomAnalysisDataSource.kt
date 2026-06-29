package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.AnalysisSeverity
import com.inclinic.app.core.model.RecommendedDoctor
import com.inclinic.app.core.model.SymptomAnalysis
import com.inclinic.app.core.model.SymptomSearchResult
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class TriageRequestDto(
    val message: String,
    val mode: String = "triage",
)

@Serializable
private data class TriageResponseDto(
    val analysis: AnalysisDto? = null,
    val doctors: List<RecommendedDoctorDto> = emptyList(),
)

@Serializable
private data class AnalysisDto(
    val summary: String = "",
    val possibleCondition: String? = null,
    val recommendedSpecialties: List<String> = emptyList(),
    val severity: String = "LOW",
)

@Serializable
private data class RecommendedDoctorDto(
    val doctorId: String = "",
    val name: String = "",
    val specialty: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val distance: String? = null,
    val matchPercentage: Int = 0,
    val availableToday: Boolean = false,
    val nextAvailable: String? = null,
)

class KtorSymptomAnalysisDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : SymptomAnalysisDataSource {

    override suspend fun analyzeSymptoms(symptoms: String): Result<SymptomSearchResult> = runCatching {
        val response = client.post("$baseUrl/api/assistant/chat") {
            contentType(ContentType.Application.Json)
            setBody(TriageRequestDto(message = symptoms))
        }
        val envelope = response.body<ApiEnvelope<TriageResponseDto>>()
        val dto = envelope.data ?: error("Empty response from triage endpoint")
        dto.toDomain()
    }

    private fun TriageResponseDto.toDomain(): SymptomSearchResult {
        val analysisDto = analysis ?: AnalysisDto()
        return SymptomSearchResult(
            analysis = SymptomAnalysis(
                summary = analysisDto.summary,
                possibleCondition = analysisDto.possibleCondition,
                recommendedSpecialties = analysisDto.recommendedSpecialties,
                severity = runCatching {
                    AnalysisSeverity.valueOf(analysisDto.severity.uppercase())
                }.getOrDefault(AnalysisSeverity.LOW),
            ),
            doctors = doctors.map { it.toDomain() },
        )
    }

    private fun RecommendedDoctorDto.toDomain() = RecommendedDoctor(
        doctorId = doctorId,
        name = name,
        specialty = specialty,
        rating = rating,
        reviewCount = reviewCount,
        distance = distance,
        matchPercentage = matchPercentage,
        availableToday = availableToday,
        nextAvailable = nextAvailable,
    )
}
