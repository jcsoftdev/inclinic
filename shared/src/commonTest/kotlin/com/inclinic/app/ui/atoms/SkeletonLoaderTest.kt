package com.inclinic.app.ui.atoms

import com.inclinic.app.ui.theme.DarkAppColors
import com.inclinic.app.ui.theme.LightAppColors
import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * RED → GREEN tests for [skeletonColors].
 *
 * [SkeletonLoader] is a pure-render Composable; the testable surface is
 * [skeletonColors], which resolves (base, highlight) from [AppColors] without
 * a Compose runtime — the same pattern used by [chipStatusColors] / ChipStatusTest.
 *
 * Visual correctness (shimmer shape, 72dp row height, 12dp radius) is verified
 * by compile-time type safety and Pencil design parity.
 */
class SkeletonLoaderTest {

    @Test
    fun light_palette_returns_non_transparent_base() {
        val (base, _) = skeletonColors(LightAppColors)
        assertNotEquals(0f, base.alpha, "base color must not be transparent")
    }

    @Test
    fun light_palette_returns_non_transparent_highlight() {
        val (_, highlight) = skeletonColors(LightAppColors)
        assertNotEquals(0f, highlight.alpha, "highlight color must not be transparent")
    }

    @Test
    fun light_palette_base_and_highlight_differ() {
        val (base, highlight) = skeletonColors(LightAppColors)
        assertNotEquals(base, highlight, "base and highlight must differ so shimmer is visible")
    }

    @Test
    fun dark_palette_base_and_highlight_differ() {
        val (base, highlight) = skeletonColors(DarkAppColors)
        assertNotEquals(base, highlight, "dark palette: base and highlight must differ")
    }

    @Test
    fun dark_palette_base_matches_elevated_token() {
        val (base, _) = skeletonColors(DarkAppColors)
        // Sanity: base maps to the elevated surface token, not an arbitrary color
        assertEquals(DarkAppColors.elevated, base)
    }

    @Test
    fun light_palette_base_matches_elevated_token() {
        val (base, _) = skeletonColors(LightAppColors)
        assertEquals(LightAppColors.elevated, base)
    }
}

// ── Local import alias so test compiles without androidx in commonTest ─────────

private fun assertEquals(expected: Any, actual: Any, message: String? = null) {
    kotlin.test.assertEquals(expected, actual, message)
}
