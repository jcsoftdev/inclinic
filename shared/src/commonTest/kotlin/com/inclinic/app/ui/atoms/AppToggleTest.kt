package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

// Ignored: runComposeUiTest requires Robolectric which is not configured in this module.
@Ignore
@OptIn(ExperimentalTestApi::class)
class AppToggleTest {

    @Test
    fun unchecked_renders_without_crash() = runComposeUiTest {
        setContent {
            AppTheme {
                AppToggle(checked = false, onCheckedChange = {})
            }
        }
        onNodeWithTag("AppToggle").assertIsDisplayed()
    }

    @Test
    fun checked_renders_without_crash() = runComposeUiTest {
        setContent {
            AppTheme {
                AppToggle(checked = true, onCheckedChange = {})
            }
        }
        onNodeWithTag("AppToggle").assertIsDisplayed()
    }

    @Test
    fun click_when_unchecked_fires_true() = runComposeUiTest {
        val received = mutableListOf<Boolean>()
        setContent {
            AppTheme {
                AppToggle(checked = false, onCheckedChange = { received.add(it) })
            }
        }
        onNodeWithTag("AppToggle").performClick()
        assertEquals(listOf(true), received)
    }

    @Test
    fun click_when_checked_fires_false() = runComposeUiTest {
        val received = mutableListOf<Boolean>()
        setContent {
            AppTheme {
                AppToggle(checked = true, onCheckedChange = { received.add(it) })
            }
        }
        onNodeWithTag("AppToggle").performClick()
        assertEquals(listOf(false), received)
    }

    @Test
    fun disabled_does_not_fire_callback() = runComposeUiTest {
        val received = mutableListOf<Boolean>()
        setContent {
            AppTheme {
                AppToggle(checked = false, onCheckedChange = { received.add(it) }, enabled = false)
            }
        }
        onNodeWithTag("AppToggle").performClick()
        assertEquals(emptyList(), received)
    }
}
