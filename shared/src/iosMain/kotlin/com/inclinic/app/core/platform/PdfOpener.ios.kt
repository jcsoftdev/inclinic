package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * Abre el PDF en iOS escribiéndolo a un archivo temporal y presentando el share sheet
 * ([UIActivityViewController]), que permite previsualizar/abrir en el visor del sistema.
 *
 * NOTA: compila, pero el interop UIKit requiere prueba en device/simulador real.
 */
@Composable
actual fun rememberPdfOpener(): PdfOpener {
    return remember {
        object : PdfOpener {
            override fun open(bytes: ByteArray, fileName: String) {
                val path = NSTemporaryDirectory() + fileName.ifBlank { "documento.pdf" }
                val url = NSURL.fileURLWithPath(path)
                bytes.writeTo(url)
                val activity = UIActivityViewController(
                    activityItems = listOf(url),
                    applicationActivities = null,
                )
                topViewController()?.presentViewController(activity, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.writeTo(url: NSURL) {
    val data: NSData = if (isEmpty()) {
        NSData()
    } else {
        usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }
    data.writeToURL(url, atomically = true)
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
