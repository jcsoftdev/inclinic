package com.inclinic.app.features.patient.search.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.SymptomSearchResult
import com.inclinic.app.features.patient.infrastructure.remote.SymptomAnalysisDataSource
import kotlinx.coroutines.withContext

class AnalyzeSymptomsUseCase(
    private val dataSource: SymptomAnalysisDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(symptoms: String): Result<SymptomSearchResult> =
        withContext(dispatchers.io) { dataSource.analyzeSymptoms(symptoms) }
}
