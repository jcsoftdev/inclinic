package com.inclinic.app.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inclinic.app.MainActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E smoke tests: patient happy path.
 *
 * IMPORTANT: These tests require a running staging backend and an Android emulator.
 * They are @Ignore in CI — run manually against a real or stubbed staging environment.
 *
 * Semantic tags used on UI nodes:
 *   LoginScreen
 *     "login_email_field"        → email BasicTextField (AppTextField.inputTestTag)
 *     "login_password_field"     → password BasicTextField
 *     "login_button"             → submit AppButton
 *   PatientHomeScreen
 *     "patient_home"             → root Box
 *   DoctorSearchScreen
 *     "doctor_result_card"       → each doctor result card Column
 *   ConsultTypeScreen
 *     "consult_type_continue_button" → "Continuar" CTA Box
 *   AvailabilityCalendarScreen
 *     "calendar_day"             → each clickable day cell Box
 *     "slot_chip"                → each time-slot chip Box
 *     "calendar_continue_button" → "Confirmar Horario" CTA Box
 *   BookingScreen (Confirmar Cita)
 *     "booking_hora_value"       → value Text inside the "Hora" DetailRow
 *
 * Regression guard for PR-1 fix: the "Hora" row used to always render "—"
 * because startTime was not propagated from the selected slot.
 * [patientCanCompleteBookingAndVerifyHoraIsNotDash] asserts it now shows the real time.
 */
@RunWith(AndroidJUnit4::class)
@Ignore("Requires a running staging backend + emulator — run manually")
class PatientBookingE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun login(email: String, password: String) {
        composeTestRule.onNodeWithTag("login_email_field").performTextInput(email)
        composeTestRule.onNodeWithTag("login_password_field").performTextInput(password)
        composeTestRule.onNodeWithTag("login_button").performClick()
    }

    private fun waitForTag(tag: String, timeoutMs: Long = 8_000L) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
            composeTestRule
                .onAllNodesWithTag(tag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /**
     * Original smoke: patient can login and land on home screen.
     */
    @Test
    fun patientCanLoginAndViewHome() {
        login("paciente@test.com", "patient123")
        waitForTag("patient_home")
    }

    /**
     * Regression guard for the Confirmar Cita "Hora" fix (PR-1).
     *
     * Flow: login → home → search doctors → select first doctor →
     *       confirm consult type → select first available date →
     *       select first slot → advance to Confirmar Cita →
     *       assert "booking_hora_value" is NOT "—".
     *
     * If this assertion fails the startTime propagation is broken again.
     */
    @Test
    fun patientCanCompleteBookingAndVerifyHoraIsNotDash() {
        // 1. Login as patient
        login("paciente@test.com", "patient123")
        waitForTag("patient_home")

        // 2. Tap "Buscar Doctores" quick action
        composeTestRule.onNodeWithTag("patient_home")
        // The quick-action card shows text "Buscar Doctores" — tap it
        composeTestRule
            .onAllNodesWithTag("patient_home")
            .fetchSemanticsNodes() // ensure home is rendered

        // Navigate via the "Buscar Doctores" label text
        composeTestRule
            .onNodeWithTag("patient_home")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.TestTag))

        // Tap the first visible doctor result card (may require scrolling in a real run)
        waitForTag("doctor_result_card")
        composeTestRule.onAllNodesWithTag("doctor_result_card")[0].performClick()

        // 3. ConsultTypeScreen: accept default consult type and continue
        waitForTag("consult_type_continue_button")
        composeTestRule.onNodeWithTag("consult_type_continue_button").performClick()

        // 4. AvailabilityCalendarScreen: select the first available day
        waitForTag("calendar_day", timeoutMs = 8_000L)
        composeTestRule.onAllNodesWithTag("calendar_day")[0].performClick()

        // 5. Select the first available time slot
        waitForTag("slot_chip", timeoutMs = 5_000L)
        composeTestRule.onAllNodesWithTag("slot_chip")[0].performClick()

        // 6. Advance to Confirmar Cita
        composeTestRule.onNodeWithTag("calendar_continue_button").performClick()

        // 7. Assert: the "Hora" value on the booking confirmation screen is NOT "—".
        //    This is the regression guard for the PR-1 startTime fix.
        waitForTag("booking_hora_value")
        composeTestRule
            .onNodeWithTag("booking_hora_value")
            .assert(
                SemanticsMatcher("booking_hora_value must not be '—'") { node ->
                    val texts = node.config.getOrElseNullable(SemanticsProperties.Text) { null }
                    texts?.any { it.text != "—" } == true
                }
            )
    }
}
