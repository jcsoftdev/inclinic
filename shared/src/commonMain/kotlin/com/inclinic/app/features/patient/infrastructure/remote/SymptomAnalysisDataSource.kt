package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.SymptomSearchResult

interface SymptomAnalysisDataSource {
    suspend fun analyzeSymptoms(symptoms: String): Result<SymptomSearchResult>
}
