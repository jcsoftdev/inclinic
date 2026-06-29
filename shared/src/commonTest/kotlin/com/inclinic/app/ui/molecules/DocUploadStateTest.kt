package com.inclinic.app.ui.molecules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [docUploadLabel].
 *
 * Pure helper extracted from DocumentUploader to enable unit testing without
 * a Compose runtime.
 */
class DocUploadStateTest {

    @Test
    fun empty_state_returns_hint() {
        val result = docUploadLabel(DocUploadState.Empty, hint = "Subir archivo")
        assertEquals("Subir archivo", result)
    }

    @Test
    fun uploading_state_returns_percentage_label() {
        val result = docUploadLabel(DocUploadState.Uploading(0.5f), hint = "Subir archivo")
        assertTrue(result.contains("50"), "Label should include percentage: $result")
    }

    @Test
    fun uploading_zero_shows_zero_percent() {
        val result = docUploadLabel(DocUploadState.Uploading(0f), hint = "Subir")
        assertTrue(result.contains("0"), "Should show 0%: $result")
    }

    @Test
    fun uploading_full_shows_hundred_percent() {
        val result = docUploadLabel(DocUploadState.Uploading(1f), hint = "Subir")
        assertTrue(result.contains("100"), "Should show 100%: $result")
    }

    @Test
    fun done_state_returns_file_name() {
        val result = docUploadLabel(DocUploadState.Done("informe.pdf"), hint = "Subir archivo")
        assertEquals("informe.pdf", result)
    }

    @Test
    fun error_state_returns_error_message() {
        val result = docUploadLabel(DocUploadState.Error("Tamaño excedido"), hint = "Subir archivo")
        assertEquals("Tamaño excedido", result)
    }

    @Test
    fun done_with_long_name_returns_full_name() {
        val longName = "historia_clinica_2024_enero_paciente.pdf"
        val result = docUploadLabel(DocUploadState.Done(longName), hint = "Subir")
        assertEquals(longName, result)
    }
}
