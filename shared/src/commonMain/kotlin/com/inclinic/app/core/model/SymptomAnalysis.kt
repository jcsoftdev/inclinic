package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AnalysisSeverity { LOW, MEDIUM, HIGH, EMERGENCY }

@Serializable
data class SymptomAnalysis(
    val summary: String,
    val possibleCondition: String?,
    val recommendedSpecialties: List<String>,
    val severity: AnalysisSeverity,
)

@Serializable
data class RecommendedDoctor(
    val doctorId: String,
    val name: String,
    val specialty: String,
    val rating: Double,
    val reviewCount: Int,
    val distance: String?,
    val matchPercentage: Int,
    val availableToday: Boolean,
    val nextAvailable: String?,
)

@Serializable
data class SymptomSearchResult(
    val analysis: SymptomAnalysis,
    val doctors: List<RecommendedDoctor>,
)
