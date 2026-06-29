package com.inclinic.app.ui.templates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Full-height scroll scaffold for auth flows.
 * Sand background, horizontally centered column, max 480dp.
 * Content receives ColumnScope so Modifier.weight() works for spacers.
 */
@Composable
fun AuthScaffold(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color    = AppTheme.colors.sand,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier            = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState()),
            ) {
                content()
            }
        }
    }
}
