package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Ignore
import kotlin.test.Test

// Ignored: runComposeUiTest requires Robolectric which is not configured in this module.
@Ignore
@OptIn(ExperimentalTestApi::class)
class StarRatingTest {

    @Test
    fun renders_without_crash_and_exposes_semantics() = runComposeUiTest {
        setContent {
            AppTheme {
                StarRating(rating = 3, max = 5)
            }
        }
        onNodeWithContentDescription("3 of 5 stars").assertIsDisplayed()
    }

    @Test
    fun rating_zero_shows_zero_of_max() = runComposeUiTest {
        setContent {
            AppTheme {
                StarRating(rating = 0, max = 5)
            }
        }
        onNodeWithContentDescription("0 of 5 stars").assertIsDisplayed()
    }

    @Test
    fun full_rating_equals_max() = runComposeUiTest {
        setContent {
            AppTheme {
                StarRating(rating = 5, max = 5)
            }
        }
        onNodeWithContentDescription("5 of 5 stars").assertIsDisplayed()
    }

    @Test
    fun custom_max_reflects_in_semantics() = runComposeUiTest {
        setContent {
            AppTheme {
                StarRating(rating = 2, max = 3)
            }
        }
        onNodeWithContentDescription("2 of 3 stars").assertIsDisplayed()
    }
}
