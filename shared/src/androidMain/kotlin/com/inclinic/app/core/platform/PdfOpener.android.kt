package com.inclinic.app.core.platform

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberPdfOpener(): PdfOpener {
    val context = LocalContext.current
    return remember {
        object : PdfOpener {
            override fun open(bytes: ByteArray, fileName: String) {
                val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
                val file = File(dir, fileName.ifBlank { "documento.pdf" })
                file.writeBytes(bytes)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                val view = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(view, "Abrir PDF").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        }
    }
}
