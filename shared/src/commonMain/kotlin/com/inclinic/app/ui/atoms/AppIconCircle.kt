package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Colored circle with a centered icon — used for illustration sections in auth screens.
 *
 * @param icon        The icon to display inside.
 * @param bgColor     Background color of the circle.
 * @param iconTint    Tint for the icon.
 * @param circleSize  Diameter of the circle (default 96dp).
 * @param iconSize    Size of the icon (default 44dp).
 */
@Composable
fun AppIconCircle(
    icon: ImageVector,
    bgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    circleSize: Dp = 96.dp,
    iconSize: Dp = 44.dp,
    contentDescription: String? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(bgColor),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint               = iconTint,
            modifier           = Modifier.size(iconSize),
        )
    }
}
