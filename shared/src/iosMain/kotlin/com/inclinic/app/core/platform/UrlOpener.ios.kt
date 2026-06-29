package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * Abre una URL en iOS usando [UIApplication.sharedApplication.openURL].
 *
 * NOTA: compila, pero el interop UIKit requiere prueba en device/simulador real.
 */
@Composable
actual fun rememberUrlOpener(): UrlOpener {
    return remember {
        object : UrlOpener {
            override fun open(url: String) {
                val nsUrl = NSURL.URLWithString(url) ?: return
                @Suppress("DEPRECATION")
                UIApplication.sharedApplication.openURL(nsUrl)
            }
        }
    }
}
