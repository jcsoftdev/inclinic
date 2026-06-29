package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Ignore
import kotlin.test.Test

// Ignored: runComposeUiTest requires Robolectric which is not configured in this module.
@Ignore
@OptIn(ExperimentalTestApi::class)
class InfoBannerTest {

    @Test
    fun title_and_description_displayed_for_info_tone() = runComposeUiTest {
        setContent {
            AppTheme {
                InfoBanner(title = "Información", description = "Esto es informativo")
            }
        }
        onNodeWithText("Información").assertIsDisplayed()
        onNodeWithText("Esto es informativo").assertIsDisplayed()
    }

    @Test
    fun renders_for_all_tones() = runComposeUiTest {
        InfoBannerTone.entries.forEach { tone ->
            setContent {
                AppTheme {
                    InfoBanner(
                        title       = "Título $tone",
                        description = "Descripción $tone",
                        tone        = tone,
                    )
                }
            }
            onNodeWithText("Título $tone").assertIsDisplayed()
        }
    }

    @Test
    fun success_tone_shows_correct_texts() = runComposeUiTest {
        setContent {
            AppTheme {
                InfoBanner(
                    title       = "Éxito",
                    description = "Operación completada",
                    tone        = InfoBannerTone.Success,
                )
            }
        }
        onNodeWithText("Éxito").assertIsDisplayed()
        onNodeWithText("Operación completada").assertIsDisplayed()
    }

    @Test
    fun error_tone_shows_correct_texts() = runComposeUiTest {
        setContent {
            AppTheme {
                InfoBanner(
                    title       = "Error",
                    description = "Algo salió mal",
                    tone        = InfoBannerTone.Error,
                )
            }
        }
        onNodeWithText("Error").assertIsDisplayed()
        onNodeWithText("Algo salió mal").assertIsDisplayed()
    }
}
