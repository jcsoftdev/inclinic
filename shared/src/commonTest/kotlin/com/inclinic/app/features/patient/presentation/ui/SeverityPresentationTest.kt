package com.inclinic.app.features.patient.presentation.ui

import com.inclinic.app.core.model.AnalysisSeverity
import com.inclinic.app.ui.theme.DarkAppColors
import com.inclinic.app.ui.theme.LightAppColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the severity → label/color/CTA pure functions used by
 * [SymptomResultsScreen]. Covers the design gap where EMERGENCY rendered
 * as a raw untranslated enum name with no urgent-care call-to-action.
 */
class SeverityPresentationTest {

    // ── severityLabel ────────────────────────────────────────────────────────

    @Test
    fun low_severity_maps_to_leve() {
        assertEquals("Leve", severityLabel(AnalysisSeverity.LOW))
    }

    @Test
    fun medium_severity_maps_to_media() {
        assertEquals("Media", severityLabel(AnalysisSeverity.MEDIUM))
    }

    @Test
    fun high_severity_maps_to_alta() {
        assertEquals("Alta", severityLabel(AnalysisSeverity.HIGH))
    }

    @Test
    fun emergency_severity_maps_to_emergencia() {
        assertEquals("Emergencia", severityLabel(AnalysisSeverity.EMERGENCY))
    }

    @Test
    fun no_severity_label_leaks_the_raw_enum_name() {
        for (severity in AnalysisSeverity.entries) {
            assertNotEquals(severity.name, severityLabel(severity))
        }
    }

    // ── severityColor ────────────────────────────────────────────────────────

    @Test
    fun emergency_severity_uses_the_alarm_error_color_in_light_theme() {
        assertEquals(LightAppColors.error, severityColor(AnalysisSeverity.EMERGENCY, LightAppColors))
    }

    @Test
    fun emergency_severity_uses_the_alarm_error_color_in_dark_theme() {
        assertEquals(DarkAppColors.error, severityColor(AnalysisSeverity.EMERGENCY, DarkAppColors))
    }

    @Test
    fun low_severity_does_not_use_the_alarm_error_color() {
        assertNotEquals(LightAppColors.error, severityColor(AnalysisSeverity.LOW, LightAppColors))
    }

    // ── shouldShowUrgentCareNotice ──────────────────────────────────────────

    @Test
    fun emergency_severity_shows_urgent_care_notice() {
        assertTrue(shouldShowUrgentCareNotice(AnalysisSeverity.EMERGENCY))
    }

    @Test
    fun high_severity_shows_urgent_care_notice() {
        assertTrue(shouldShowUrgentCareNotice(AnalysisSeverity.HIGH))
    }

    @Test
    fun medium_severity_does_not_show_urgent_care_notice() {
        assertFalse(shouldShowUrgentCareNotice(AnalysisSeverity.MEDIUM))
    }

    @Test
    fun low_severity_does_not_show_urgent_care_notice() {
        assertFalse(shouldShowUrgentCareNotice(AnalysisSeverity.LOW))
    }

    // ── urgentCareNoticeMessage ──────────────────────────────────────────────

    @Test
    fun emergency_severity_has_a_non_null_urgent_care_message() {
        assertNotNull(urgentCareNoticeMessage(AnalysisSeverity.EMERGENCY))
    }

    @Test
    fun high_severity_has_a_non_null_urgent_care_message() {
        assertNotNull(urgentCareNoticeMessage(AnalysisSeverity.HIGH))
    }

    @Test
    fun low_and_medium_severity_have_no_urgent_care_message() {
        assertNull(urgentCareNoticeMessage(AnalysisSeverity.LOW))
        assertNull(urgentCareNoticeMessage(AnalysisSeverity.MEDIUM))
    }
}
