package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.ui.graphics.Color
import com.inclinic.app.core.model.AnalysisSeverity
import com.inclinic.app.ui.theme.AppColors

/**
 * Pure severity → UI copy/color mapping for [SymptomResultsScreen].
 *
 * Kept outside any `@Composable` so the mapping is unit-testable without a
 * Compose test harness. Every label is Peruvian-Spanish tuteo copy — the raw
 * [AnalysisSeverity] enum name must never leak into the UI.
 */
fun severityLabel(severity: AnalysisSeverity): String = when (severity) {
    AnalysisSeverity.LOW -> "Leve"
    AnalysisSeverity.MEDIUM -> "Media"
    AnalysisSeverity.HIGH -> "Alta"
    AnalysisSeverity.EMERGENCY -> "Emergencia"
}

/**
 * Severity badge accent color. EMERGENCY uses the design system's alarm/error
 * red so it reads as urgent at a glance; every other severity keeps the
 * neutral white text already used on the navy analysis card.
 */
fun severityColor(severity: AnalysisSeverity, colors: AppColors): Color = when (severity) {
    AnalysisSeverity.EMERGENCY -> colors.error
    else -> Color.White
}

/**
 * True when the results screen should render the urgent-care alert card.
 *
 * Decision: HIGH is included alongside EMERGENCY — both indicate the patient
 * should be prompted toward in-person care rather than only self-triage via
 * chat, they differ only in urgency copy (see [urgentCareNoticeMessage]).
 */
fun shouldShowUrgentCareNotice(severity: AnalysisSeverity): Boolean =
    severity == AnalysisSeverity.HIGH || severity == AnalysisSeverity.EMERGENCY

/**
 * Copy for the urgent-care alert card, or `null` when
 * [shouldShowUrgentCareNotice] is false for [severity] — callers should not
 * render a card in that case.
 */
fun urgentCareNoticeMessage(severity: AnalysisSeverity): String? = when (severity) {
    AnalysisSeverity.EMERGENCY ->
        "Busca atención médica de emergencia de inmediato. No esperes: acude al servicio de urgencias más cercano o llama a emergencias."
    AnalysisSeverity.HIGH ->
        "Te recomendamos buscar atención médica presencial cuanto antes para una evaluación oportuna."
    else -> null
}
