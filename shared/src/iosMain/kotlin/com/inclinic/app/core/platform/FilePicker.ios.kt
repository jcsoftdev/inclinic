package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * Picker de archivos para iOS basado en [UIDocumentPickerViewController], limitado a
 * imágenes y PDF (los MIME que acepta `/api/upload`). Lee el archivo elegido a bytes.
 *
 * NOTA: compila, pero requiere prueba en device/simulador real — el interop UIKit no se
 * valida en la compilación de CI.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberFilePicker(onPicked: (PickedFile?) -> Unit): FilePickerLauncher {
    val delegate = remember { DocumentPickerDelegate(onPicked) }
    return remember(delegate) {
        object : FilePickerLauncher {
            override fun launch() {
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypeImage, UTTypePDF),
                    asCopy = true,
                )
                picker.delegate = delegate
                topViewController()?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class DocumentPickerDelegate(
    private val onPicked: (PickedFile?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onPicked(null)
            return
        }
        val scoped = url.startAccessingSecurityScopedResource()
        val data = NSData.dataWithContentsOfURL(url)
        if (scoped) url.stopAccessingSecurityScopedResource()
        if (data == null) {
            onPicked(null)
            return
        }
        val fileName = url.lastPathComponent ?: "adjunto"
        onPicked(PickedFile(data.toByteArray(), fileName, mimeForExtension(url.pathExtension)))
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val out = ByteArray(size)
    out.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    return out
}

private fun mimeForExtension(ext: String?): String = when (ext?.lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "heic" -> "image/heic"
    "webp" -> "image/webp"
    "pdf" -> "application/pdf"
    else -> "application/octet-stream"
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
