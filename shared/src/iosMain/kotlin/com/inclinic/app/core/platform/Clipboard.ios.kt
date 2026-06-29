package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

/**
 * Acceso al portapapeles en iOS vía [UIPasteboard.generalPasteboard].
 *
 * NOTA: compila, pero el interop UIKit requiere prueba en device/simulador real.
 */
@Composable
actual fun rememberClipboard(): Clipboard {
    return remember {
        object : Clipboard {
            override fun copy(text: String, label: String?) {
                UIPasteboard.generalPasteboard.string = text
            }
        }
    }
}
