package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * RED → GREEN tests for [AppAvatar].
 *
 * Split into two suites:
 *   1. Pure unit tests for [avatarInitials] — no Compose runtime required.
 *   2. Compose UI tests for the rendered component — initials vs. image branch.
 *
 * ## avatarInitials rule (single source of truth)
 *   - Blank / empty  → ""
 *   - Two+ tokens    → first char of first token + first char of second token, uppercase
 *   - Single token   → first 2 chars uppercase (or 1 if the token has only 1 char)
 */
class AppAvatarInitialsTest {

    // ── two-token cases ──────────────────────────────────────────────────────

    @Test
    fun two_tokens_returns_first_chars_uppercased() {
        assertEquals("JP", avatarInitials("Juan Pérez"))
    }

    @Test
    fun two_tokens_already_uppercase_stays_uppercase() {
        assertEquals("JP", avatarInitials("JUAN PEREZ"))
    }

    @Test
    fun extra_spaces_are_collapsed_before_splitting() {
        assertEquals("AM", avatarInitials("  ana  maria "))
    }

    @Test
    fun three_or_more_tokens_uses_only_first_two() {
        assertEquals("JA", avatarInitials("Juan Antonio Pérez"))
    }

    // ── single-token cases ───────────────────────────────────────────────────

    @Test
    fun single_token_returns_first_two_chars_uppercased() {
        assertEquals("JU", avatarInitials("Juan"))
    }

    @Test
    fun single_one_char_token_returns_one_char() {
        assertEquals("X", avatarInitials("x"))
    }

    @Test
    fun single_two_char_token_returns_both_chars() {
        assertEquals("AB", avatarInitials("ab"))
    }

    // ── empty / blank cases ──────────────────────────────────────────────────

    @Test
    fun empty_string_returns_empty() {
        assertEquals("", avatarInitials(""))
    }

    @Test
    fun blank_string_returns_empty() {
        assertEquals("", avatarInitials("   "))
    }
}

/**
 * Compose UI tests for [AppAvatar] — exercises the initials branch and the image branch.
 */
@OptIn(ExperimentalTestApi::class)
class AppAvatarComposeTest {

    @Test
    fun initials_branch_shows_correct_text_when_no_image_url() = runComposeUiTest {
        setContent {
            AppTheme {
                AppAvatar(name = "Juan Pérez", imageUrl = null)
            }
        }
        onNodeWithText("JP").assertIsDisplayed()
    }

    @Test
    fun image_branch_does_not_show_initials_text() = runComposeUiTest {
        setContent {
            AppTheme {
                AppAvatar(name = "Juan Pérez", imageUrl = "https://example.com/avatar.png")
            }
        }
        // Initials must NOT be present when imageUrl is supplied
        onNodeWithText("JP").assertDoesNotExist()
    }

    @Test
    fun image_branch_exposes_content_description_equal_to_name() = runComposeUiTest {
        setContent {
            AppTheme {
                AppAvatar(name = "Juan Pérez", imageUrl = "https://example.com/avatar.png")
            }
        }
        // The AsyncImage is rendered with contentDescription = name
        onNodeWithContentDescription("Juan Pérez").assertExists()
    }
}
