package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/** Escribe texto en el portapapeles del sistema. */
interface Clipboard {
    fun copy(text: String, label: String? = null)
}

/**
 * Recuerda un acceso al portapapeles nativo por plataforma.
 * Usar [Clipboard.copy] para copiar texto al portapapeles del sistema.
 */
@Composable
expect fun rememberClipboard(): Clipboard
