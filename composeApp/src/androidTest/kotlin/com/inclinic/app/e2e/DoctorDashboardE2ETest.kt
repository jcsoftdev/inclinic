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
 * IMPORTANT: This test requires a running staging backend. It is @Ignore in CI
 * and must be run manually against a real or stubbed staging environment.
 *
 * Semantic tags used on UI nodes:
 *   - "login_email_field" → login email BasicTextField (AppTextField.inputTestTag)
 *   - "login_password_field" → login password BasicTextField
 *   - "login_button"      → login submit AppButton
 *   - "doctor_dashboard"  → root PullToRefreshBox of DoctorDashboardScreen
 */
@RunWith(AndroidJUnit4::class)
@Ignore("Requires a running staging backend — run manually")
class DoctorDashboardE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun doctorCanLoginAndViewDashboard() {
        composeTestRule.onNodeWithTag("login_email_field").performTextInput("ana.torres@test.com")
        composeTestRule.onNodeWithTag("login_password_field").performTextInput("doctor123")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodesWithTag("doctor_dashboard")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
