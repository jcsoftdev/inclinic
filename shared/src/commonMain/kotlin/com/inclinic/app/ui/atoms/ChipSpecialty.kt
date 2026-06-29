package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Specialty filter chip atom matching the Pencil component/ChipSpecialty.
 *
 * - Unselected: teal bg + teal text (matches the Pencil default).
 * - Selected: navy bg + white text (inverted for selection feedback).
 * - [onClick] is optional — omit for read-only display.
 */
@Composable
fun ChipSpecialty(
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val bgColor   = if (selected) colors.navy else colors.tealBg
    val textColor = if (selected) Color.White else colors.teal

    Text(
        text       = label,
        color      = textColor,
        fontSize   = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier   = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onClick,
                    )
                } else Modifier,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
