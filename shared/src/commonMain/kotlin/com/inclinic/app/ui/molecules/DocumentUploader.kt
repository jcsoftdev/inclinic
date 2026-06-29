package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

// ── Model ─────────────────────────────────────────────────────────────────────

sealed interface DocUploadState {
    data object Empty                                  : DocUploadState
    data class  Uploading(val progress: Float)         : DocUploadState
    data class  Done(val fileName: String)             : DocUploadState
    data class  Error(val message: String)             : DocUploadState
}

// ── Pure helper (unit-testable) ───────────────────────────────────────────────

/**
 * Returns the display label for the current upload state.
 * [hint] is shown when the state is [DocUploadState.Empty].
 */
fun docUploadLabel(state: DocUploadState, hint: String): String = when (state) {
    is DocUploadState.Empty          -> hint
    is DocUploadState.Uploading      -> "Subiendo… ${(state.progress * 100).toInt()}%"
    is DocUploadState.Done           -> state.fileName
    is DocUploadState.Error          -> state.message
}

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * Document upload area with label, hint, and state-driven visual feedback.
 *
 * @param label       Field label shown above the upload area.
 * @param hint        Placeholder text shown in the [DocUploadState.Empty] state.
 * @param state       Current upload state driving the visual presentation.
 * @param onPickClick Called when the user taps to pick a file.
 */
@Composable
fun DocumentUploader(
    label: String,
    hint: String,
    state: DocUploadState,
    onPickClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    val borderColor = when (state) {
        is DocUploadState.Error -> colors.red
        is DocUploadState.Done  -> colors.green
        else                    -> colors.lavLight
    }
    val labelText = docUploadLabel(state, hint)

    Column(modifier = modifier) {
        Text(
            text  = label,
            style = AppTheme.typography.label,
            color = colors.muted,
        )
        Spacer(Modifier.height(dimens.spacingXs))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(colors.lav50, AppTheme.shapes.medium)
                .border(dimens.borderWidth, borderColor, AppTheme.shapes.medium)
                .clickable(onClick = onPickClick)
                .padding(horizontal = dimens.spacingMd),
        ) {
            when (state) {
                is DocUploadState.Uploading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress       = { state.progress },
                            color          = colors.navy,
                            trackColor     = colors.lavLight,
                            modifier       = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = labelText,
                            style = AppTheme.typography.subtitle,
                            color = colors.muted,
                        )
                    }
                }
                else -> {
                    val textColor = when (state) {
                        is DocUploadState.Error -> colors.red
                        is DocUploadState.Done  -> colors.green
                        else                    -> colors.muted
                    }
                    Text(
                        text       = labelText,
                        style      = AppTheme.typography.subtitle,
                        color      = textColor,
                        fontWeight = if (state is DocUploadState.Done) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
