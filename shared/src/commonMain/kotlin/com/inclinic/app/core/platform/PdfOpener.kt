package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/** Abre un PDF (recibido como bytes) en el visor del sistema. */
interface PdfOpener {
    fun open(bytes: ByteArray, fileName: String)
}

/**
 * Recuerda un abridor de PDF nativo por plataforma. Escribe los bytes a un archivo
 * temporal y lo abre con el visor/compartir del sistema. Se usa para abrir el PDF de
 * receta descargado de forma autenticada (el Bearer token no viaja en un open de URL).
 */
@Composable
expect fun rememberPdfOpener(): PdfOpener
