package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Labeled text input matching the design.pen spec:
 * - White background, 1px border (#DDE1F0), 10px radius, 50-52dp height
 * - Optional leading icon (18×18, muted fill)
 * - Focus: navyLight border; error: red border + red helper text
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    /** Semantic test tag placed on the inner [BasicTextField] node — the node that supports
     *  [performTextInput] in Compose UI tests. */
    inputTestTag: String? = null,
) {
    val colors     = AppTheme.colors
    val dimens     = AppTheme.dimens
    val typography = AppTheme.typography

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        error != null -> colors.red
        isFocused     -> colors.navyLight
        else          -> colors.border
    }

    val shape = RoundedCornerShape(dimens.radius)

    Column(modifier = modifier) {
        Text(
            text  = label,
            style = typography.label,
            color = colors.muted,
        )
        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value                = value,
            onValueChange        = onValueChange,
            enabled              = enabled,
            singleLine           = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions      = keyboardOptions,
            keyboardActions      = keyboardActions,
            interactionSource    = interactionSource,
            textStyle            = typography.body.copy(color = if (enabled) colors.text else colors.light),
            cursorBrush          = SolidColor(colors.navy),
            modifier             = (if (inputTestTag != null) Modifier.testTag(inputTestTag) else Modifier)
                .fillMaxWidth()
                .heightIn(min = dimens.inputHeight)
                .border(1.dp, borderColor, shape)
                .background(
                    color = if (enabled) colors.surface else colors.surface.copy(alpha = 0.6f),
                    shape = shape,
                ),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(
                        horizontal = dimens.inputPaddingH,
                        vertical   = dimens.inputPaddingV,
                    ),
                ) {
                    if (leadingIcon != null) {
                        Box(modifier = Modifier.size(18.dp)) { leadingIcon() }
                        Spacer(modifier = Modifier.padding(start = 10.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text      = placeholder,
                                style     = typography.body,
                                color     = colors.light,
                                textAlign = TextAlign.Start,
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                }
            },
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = error,
                style = typography.fieldError,
                color = colors.red,
            )
        }
    }
}
