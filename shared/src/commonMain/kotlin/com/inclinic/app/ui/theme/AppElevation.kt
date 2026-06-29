package com.inclinic.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppElevation(
    val none: Dp,
    val low: Dp,
    val medium: Dp,
    val high: Dp,
)

val DefaultAppElevation = AppElevation(
    none   = 0.dp,
    low    = 1.dp,
    medium = 4.dp,
    high   = 12.dp,
)
