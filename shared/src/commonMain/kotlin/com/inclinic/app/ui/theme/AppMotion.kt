package com.inclinic.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

@Immutable
data class AppMotion(
    val durationFast: Int,     // ms — micro-interactions (press feedback)
    val durationMedium: Int,   // ms — standard transitions
    val durationSlow: Int,     // ms — entering screens / large surface changes
    val easingStandard: Easing,
    val easingEmphasized: Easing,
)

val DefaultAppMotion = AppMotion(
    durationFast     = 120,
    durationMedium   = 240,
    durationSlow     = 400,
    easingStandard   = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    easingEmphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
)
