package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.inclinic.app.ui.theme.AppTheme

/**
 * Pill-shaped search bar atom.
 *
 * 48.dp height, pill shape ([AppTheme.dimens.radiusPill]), surface background, 1.5.dp border.
 * Leading Lucide [Search] icon, then a [BasicTextField] with a placeholder when [query] is empty.
 *
 * @param query          Current search text.
 * @param onQueryChange  Fired on every keystroke with the new text.
 * @param modifier       Modifier applied to the outer Row.
 * @param placeholder    Hint shown when [query] is empty (default "Buscar...").
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
) {
    val colors     = AppTheme.colors
    val dimens     = AppTheme.dimens
    val typography = AppTheme.typography

    val shape = RoundedCornerShape(dimens.radiusPill)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(colors.surface)
            .border(dimens.borderWidth, colors.border, shape)
            .padding(horizontal = 16.dp),
    ) {
        Icon(
            imageVector        = Lucide.Search,
            contentDescription = null,
            tint               = colors.light,
            modifier           = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        BasicTextField(
            value         = query,
            onValueChange = onQueryChange,
            singleLine    = true,
            textStyle     = typography.body.copy(color = colors.text),
            cursorBrush   = SolidColor(colors.navy),
            modifier      = Modifier
                .weight(1f)
                .testTag("SearchBarTextField"),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text  = placeholder,
                            style = typography.body,
                            color = colors.light,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Composable
internal fun PreviewSearchBarLight() {
    AppTheme(useDarkTheme = false) {
        SearchBar(query = "", onQueryChange = {})
    }
}

@Composable
internal fun PreviewSearchBarDark() {
    AppTheme(useDarkTheme = true) {
        SearchBar(query = "doctor García", onQueryChange = {})
    }
}
