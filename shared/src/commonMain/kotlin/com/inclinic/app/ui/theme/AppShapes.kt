package com.inclinic.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class AppShapes(
    val small: Shape,
    val medium: Shape,
    val large: Shape,
    val pill: Shape,
)

val DefaultAppShapes = AppShapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large  = RoundedCornerShape(16.dp),
    pill   = RoundedCornerShape(percent = 50),
)
