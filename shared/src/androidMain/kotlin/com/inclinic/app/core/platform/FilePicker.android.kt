package com.inclinic.app.core.platform

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFilePicker(onPicked: (PickedFile?) -> Unit): FilePickerLauncher {
    val context = LocalContext.current
    // Acepta imágenes y PDF (alineado con los MIME permitidos por el backend /api/upload).
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) {
            onPicked(null)
            return@rememberLauncherForActivityResult
        }
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val fileName = queryDisplayName(context, uri) ?: "adjunto"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) onPicked(null) else onPicked(PickedFile(bytes, fileName, mimeType))
    }

    return remember(launcher) {
        object : FilePickerLauncher {
            override fun launch() {
                // GetContent acepta un filtro MIME; usamos un patrón que cubre imágenes y PDF.
                launcher.launch("*/*")
            }
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
}
