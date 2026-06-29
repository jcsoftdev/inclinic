package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/** Abre una URL en el navegador o app del sistema. */
interface UrlOpener {
    fun open(url: String)
}

/**
 * Recuerda un abridor de URLs nativo por plataforma.
 * Usar [UrlOpener.open] para abrir una URL con el navegador/handler del sistema.
 */
@Composable
expect fun rememberUrlOpener(): UrlOpener
