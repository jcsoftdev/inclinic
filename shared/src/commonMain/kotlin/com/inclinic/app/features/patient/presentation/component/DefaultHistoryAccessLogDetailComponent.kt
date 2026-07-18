package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.model.HistoryAccessLog

/**
 * Decompose implementation of [HistoryAccessLogDetailComponent].
 *
 * Purely presentational — the entry is passed in from [HistoryAccessLogsComponent.Output.NavigateToDetail]
 * (originally the list already loaded via [com.inclinic.app.features.patient.medical_history.application.GetHistoryAccessLogsUseCase]),
 * so there is nothing to fetch here.
 */
class DefaultHistoryAccessLogDetailComponent(
    componentContext: ComponentContext,
    override val entry: HistoryAccessLog,
    private val onOutput: (HistoryAccessLogDetailComponent.Output) -> Unit,
) : HistoryAccessLogDetailComponent, ComponentContext by componentContext {

    override fun onBack() {
        onOutput(HistoryAccessLogDetailComponent.Output.Back)
    }
}
