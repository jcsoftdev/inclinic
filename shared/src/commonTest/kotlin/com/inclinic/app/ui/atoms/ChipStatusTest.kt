package com.inclinic.app.ui.atoms

import androidx.compose.ui.graphics.Color
import com.inclinic.app.ui.theme.LightAppColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * RED → GREEN tests for [chipStatusColors].
 *
 * Pure function extracted from ChipStatus to enable unit testing in commonTest.
 * Validates that every ChipStatusKind resolves to a distinct, meaningful color pair.
 */
class ChipStatusTest {

    private val palette = LightAppColors

    @Test
    fun success_returns_green_text_and_greenBg() {
        val (text, bg) = chipStatusColors(ChipStatusKind.Success, palette)
        assertEquals(palette.green, text)
        assertEquals(palette.greenBg, bg)
    }

    @Test
    fun warning_returns_amber_text_and_amberBg() {
        val (text, bg) = chipStatusColors(ChipStatusKind.Warning, palette)
        assertEquals(palette.amber, text)
        assertEquals(palette.amberBg, bg)
    }

    @Test
    fun error_returns_red_text_and_redBg() {
        val (text, bg) = chipStatusColors(ChipStatusKind.Error, palette)
        assertEquals(palette.red, text)
        assertEquals(palette.redBg, bg)
    }

    @Test
    fun info_returns_navy_text_and_navyTint() {
        val (text, bg) = chipStatusColors(ChipStatusKind.Info, palette)
        assertEquals(palette.navy, text)
        assertEquals(palette.navyTint, bg)
    }

    @Test
    fun neutral_returns_muted_text_and_lav50() {
        val (text, bg) = chipStatusColors(ChipStatusKind.Neutral, palette)
        assertEquals(palette.muted, text)
        assertEquals(palette.lav50, bg)
    }

    @Test
    fun all_kinds_produce_distinct_background_colors() {
        val bgs: List<Color> = ChipStatusKind.entries.map { chipStatusColors(it, palette).second }
        assertEquals(ChipStatusKind.entries.size, bgs.toSet().size, "Each kind must have a unique bg color")
    }

    @Test
    fun all_kinds_produce_distinct_text_colors() {
        val texts: List<Color> = ChipStatusKind.entries.map { chipStatusColors(it, palette).first }
        assertEquals(ChipStatusKind.entries.size, texts.toSet().size, "Each kind must have a unique text color")
    }
}
