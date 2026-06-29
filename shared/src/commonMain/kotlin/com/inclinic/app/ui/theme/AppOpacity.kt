package com.inclinic.app.ui.theme

import androidx.compose.runtime.Immutable

@Immutable
data class AppOpacity(
    val disabled: Float,
    val overlayScrim: Float,
    val divider: Float,
    val hover: Float,
)

val DefaultAppOpacity = AppOpacity(
    disabled     = 0.50f,
    overlayScrim = 0.25f,
    divider      = 0.40f,
    hover        = 0.08f,
)
