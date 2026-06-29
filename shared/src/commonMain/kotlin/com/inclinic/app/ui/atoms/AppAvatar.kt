package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.inclinic.app.ui.theme.AppTheme
import com.inclinic.app.ui.theme.interFamily

// ── Pure helper (extracted for unit-testability) ──────────────────────────────

/**
 * Derives up to two uppercase initials from [name].
 *
 * Rules (single source of truth — also documented in [AppAvatarInitialsTest]):
 * - Blank / empty string         → ""
 * - Two or more whitespace-separated tokens → first char of token[0] + first char of token[1], uppercase
 * - Single token                 → first two chars uppercase (one char if token length == 1)
 */
internal fun avatarInitials(name: String): String {
    val tokens = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        tokens.isEmpty() -> ""
        tokens.size >= 2 -> "${tokens[0].first().uppercaseChar()}${tokens[1].first().uppercaseChar()}"
        else -> tokens[0].take(2).uppercase()
    }
}

// ── Public API ────────────────────────────────────────────────────────────────

/**
 * Circle avatar atom.
 *
 * Renders a Coil [AsyncImage] when [imageUrl] is non-null, clipped to [CircleShape] with
 * [ContentScale.Crop]. Falls back to centered initials (derived via [avatarInitials]) on a
 * navy background when [imageUrl] is null.
 *
 * @param name      Full name used to derive initials and as [AsyncImage] contentDescription.
 * @param modifier  Modifier applied to the root [Box].
 * @param imageUrl  Remote image URL. When null the initials fallback is shown.
 * @param size      Diameter of the circle (default 40.dp). Font size scales as size × 0.36.
 */
@Composable
fun AppAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 40.dp,
) {
    val colors = AppTheme.colors

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.navy),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model              = imageUrl,
                contentDescription = name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(size)
                    .clip(CircleShape),
            )
        } else {
            Text(
                text       = avatarInitials(name),
                color      = Color.White,
                fontFamily = interFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize   = (size.value * 0.36f).sp,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────
//
// Note: `ui-tooling-preview` is not wired to shared/commonMain, so these are plain
// @Composable functions that can be called from an androidMain or iosMain preview
// wrapper. Render them in a preview host (composeApp or Xcode canvas) by wrapping
// with the platform @Preview annotation there.

@Composable
internal fun PreviewAvatarInitialsLight() {
    AppTheme(useDarkTheme = false) {
        AppAvatar(name = "Juan Pérez", size = 48.dp)
    }
}

@Composable
internal fun PreviewAvatarInitialsDark() {
    AppTheme(useDarkTheme = true) {
        AppAvatar(name = "Juan Pérez", size = 48.dp)
    }
}

@Composable
internal fun PreviewAvatarSizes() {
    AppTheme(useDarkTheme = false) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            AppAvatar(name = "Ana Torres", size = 32.dp)
            AppAvatar(name = "Ana Torres", size = 40.dp)
            AppAvatar(name = "Ana Torres", size = 48.dp)
            AppAvatar(name = "Ana Torres", size = 64.dp)
        }
    }
}

// Image branch preview — supply a real URL at runtime to exercise the Coil path:
//
// @Composable
// internal fun PreviewAvatarImageLight() {
//     AppTheme(useDarkTheme = false) {
//         AppAvatar(
//             name     = "Doctor Ríos",
//             imageUrl = "https://example.com/doctor.jpg",
//             size     = 48.dp,
//         )
//     }
// }
