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
 * Saves bytes on iOS by writing to a temporary file and presenting the system share sheet
 * ([UIActivityViewController]), which lets the user send to Files, Mail, AirDrop, etc.
 *
 * Mirrors [rememberPdfOpener] exactly — same helpers, same UIKit approach.
 */
@Composable
actual fun rememberFileSaver(): FileSaver {
    return remember {
        object : FileSaver {
            override fun saveBytes(fileName: String, mimeType: String, bytes: ByteArray) {
                val safeName = fileName.ifBlank { "export" }
                val path = NSTemporaryDirectory() + safeName
                val url = NSURL.fileURLWithPath(path)
                bytes.writeToFileUrl(url)
                val activity = UIActivityViewController(
                    activityItems = listOf(url),
                    applicationActivities = null,
                )
                fileSaverTopViewController()?.presentViewController(activity, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.writeToFileUrl(url: NSURL) {
    val data: NSData = if (isEmpty()) {
        NSData()
    } else {
        usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }
    data.writeToURL(url, atomically = true)
}

private fun fileSaverTopViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
