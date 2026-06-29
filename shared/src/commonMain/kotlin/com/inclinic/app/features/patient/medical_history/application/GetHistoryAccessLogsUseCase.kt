package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import kotlinx.coroutines.withContext

class GetHistoryAccessLogsUseCase(
    private val dataSource: MedicalRecordDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<HistoryAccessLog>> =
        withContext(dispatchers.io) { dataSource.getAccessLogs() }
}
