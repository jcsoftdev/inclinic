package com.inclinic.app.ui.atoms

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.inclinic.app.ui.theme.AppTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EmptyStateTest {

    @Test
    fun title_is_displayed() = runComposeUiTest {
        setContent {
            AppTheme {
                EmptyState(title = "Sin resultados", subtitle = "No se encontraron datos")
            }
        }
        onNodeWithText("Sin resultados").assertIsDisplayed()
    }

    @Test
    fun subtitle_is_displayed() = runComposeUiTest {
        setContent {
            AppTheme {
                EmptyState(title = "Sin resultados", subtitle = "No se encontraron datos")
            }
        }
        onNodeWithText("No se encontraron datos").assertIsDisplayed()
    }

    @Test
    fun both_texts_show_together() = runComposeUiTest {
        setContent {
            AppTheme {
                EmptyState(
                    title    = "No hay citas",
                    subtitle = "Agenda una cita para comenzar",
                )
            }
        }
        onNodeWithText("No hay citas").assertIsDisplayed()
        onNodeWithText("Agenda una cita para comenzar").assertIsDisplayed()
    }
}
