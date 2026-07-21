package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.model.HistoryAccessLog

/**
 * Presentation contract for the access-log drill-down screen.
 *
 * No async loading is needed — the [entry] is the exact [HistoryAccessLog] the user tapped
 * on [HistoryAccessLogsScreen]; `GET /api/patients/me/access-log` already returns every field
 * this screen renders (who, when, and the access type/reason), so there is no separate
 * "get single access log" endpoint to call.
 */
interface HistoryAccessLogDetailComponent {
    /** The access-log entry being shown. */
    val entry: HistoryAccessLog

    /** User tapped back / close. */
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}
