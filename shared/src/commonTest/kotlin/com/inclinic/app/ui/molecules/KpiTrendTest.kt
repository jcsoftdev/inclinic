package com.inclinic.app.ui.molecules

import androidx.compose.ui.graphics.Color
import com.inclinic.app.ui.theme.LightAppColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * RED → GREEN tests for [kpiTrendColor] and [kpiTrendIcon].
 *
 * Pure helpers extracted from KpiCard so they can be unit-tested in commonTest
 * without a Compose runtime.
 */
class KpiTrendTest {

    private val palette = LightAppColors

    @Test
    fun up_trend_color_is_green() {
        assertEquals(palette.green, kpiTrendColor(KpiTrend.Up, palette))
    }

    @Test
    fun down_trend_color_is_red() {
        assertEquals(palette.red, kpiTrendColor(KpiTrend.Down, palette))
    }

    @Test
    fun flat_trend_color_is_muted() {
        assertEquals(palette.muted, kpiTrendColor(KpiTrend.Flat, palette))
    }

    @Test
    fun null_trend_color_is_muted() {
        assertEquals(palette.muted, kpiTrendColor(null, palette))
    }

    @Test
    fun all_non_null_trends_produce_distinct_colors() {
        val colors: List<Color> = KpiTrend.entries.map { kpiTrendColor(it, palette) }
        assertEquals(KpiTrend.entries.size, colors.toSet().size, "Each trend must resolve to a unique color")
    }

    @Test
    fun up_trend_icon_is_arrow_up() {
        assertEquals("↑", kpiTrendIcon(KpiTrend.Up))
    }

    @Test
    fun down_trend_icon_is_arrow_down() {
        assertEquals("↓", kpiTrendIcon(KpiTrend.Down))
    }

    @Test
    fun flat_trend_icon_is_dash() {
        assertEquals("–", kpiTrendIcon(KpiTrend.Flat))
    }

    @Test
    fun null_trend_icon_is_empty() {
        assertEquals("", kpiTrendIcon(null))
    }

    @Test
    fun all_non_null_trends_produce_distinct_icons() {
        val icons: List<String> = KpiTrend.entries.map { kpiTrendIcon(it) }
        assertEquals(KpiTrend.entries.size, icons.toSet().size, "Each trend must have a unique icon")
    }
}
