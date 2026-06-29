package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class AppCheckboxTest {

    @Test
    fun unchecked_renders_without_crash() = runComposeUiTest {
        setContent {
            AppTheme {
                AppCheckbox(checked = false, onCheckedChange = {})
            }
        }
        onNodeWithTag("AppCheckbox").assertIsDisplayed()
    }

    @Test
    fun checked_shows_check_icon_with_content_description() = runComposeUiTest {
        setContent {
            AppTheme {
                AppCheckbox(checked = true, onCheckedChange = {})
            }
        }
        onNodeWithContentDescription("checked").assertIsDisplayed()
    }

    @Test
    fun unchecked_does_not_show_check_icon() = runComposeUiTest {
        setContent {
            AppTheme {
                AppCheckbox(checked = false, onCheckedChange = {})
            }
        }
        onNodeWithContentDescription("checked").assertDoesNotExist()
    }

    @Test
    fun click_fires_negated_value_when_unchecked() = runComposeUiTest {
        val received = mutableListOf<Boolean>()
        setContent {
            AppTheme {
                AppCheckbox(checked = false, onCheckedChange = { received.add(it) })
            }
        }
        onNodeWithTag("AppCheckbox").performClick()
        assertEquals(listOf(true), received)
    }

    @Test
    fun click_fires_negated_value_when_checked() = runComposeUiTest {
        val received = mutableListOf<Boolean>()
        setContent {
            AppTheme {
                AppCheckbox(checked = true, onCheckedChange = { received.add(it) })
            }
        }
        onNodeWithTag("AppCheckbox").performClick()
        assertEquals(listOf(false), received)
    }
}
