package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

// Ignored: runComposeUiTest requires Robolectric which is not configured in this module.
@Ignore
@OptIn(ExperimentalTestApi::class)
class SearchBarTest {

    @Test
    fun placeholder_visible_when_query_empty() = runComposeUiTest {
        setContent {
            AppTheme {
                SearchBar(query = "", onQueryChange = {}, placeholder = "Buscar...")
            }
        }
        onNodeWithText("Buscar...").assertIsDisplayed()
    }

    @Test
    fun placeholder_not_visible_when_query_non_empty() = runComposeUiTest {
        setContent {
            AppTheme {
                SearchBar(query = "doctor", onQueryChange = {}, placeholder = "Buscar...")
            }
        }
        // The placeholder text should not appear since query is non-empty
        onNodeWithText("Buscar...").assertDoesNotExist()
    }

    @Test
    fun query_text_displayed_when_non_empty() = runComposeUiTest {
        setContent {
            AppTheme {
                SearchBar(query = "doctor", onQueryChange = {})
            }
        }
        onNodeWithTag("SearchBarTextField").assertTextContains("doctor")
    }

    @Test
    fun typing_fires_onQueryChange() = runComposeUiTest {
        val received = mutableListOf<String>()
        setContent {
            AppTheme {
                SearchBar(query = "", onQueryChange = { received.add(it) })
            }
        }
        onNodeWithTag("SearchBarTextField").performTextInput("abc")
        assertEquals(true, received.isNotEmpty())
    }
}
