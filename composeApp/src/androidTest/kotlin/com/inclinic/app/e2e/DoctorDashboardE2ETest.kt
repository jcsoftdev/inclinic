package com.inclinic.app.e2e

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
 * E2E smoke test: doctor happy path (login → view dashboard).
 *
 * IMPORTANT: This test requires a running staging backend pointed to by
 * the `staging` build flavor. Mark @Ignore in CI — run manually against
 * a real or stubbed staging environment.
 *
 * Semantic tags required on UI nodes:
 *   - "email_field"      → login email TextField
 *   - "password_field"   → login password TextField
 *   - "login_button"     → login submit button
 *   - "doctor_dashboard" → root node of DoctorDashboardScreen
 */
@RunWith(AndroidJUnit4::class)
class DoctorDashboardE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Ignore("Requires staging backend")
    fun doctorCanLoginAndViewDashboard() {
        composeTestRule.onNodeWithTag("email_field").performTextInput("ana.torres@test.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("doctor123")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodesWithTag("doctor_dashboard")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
