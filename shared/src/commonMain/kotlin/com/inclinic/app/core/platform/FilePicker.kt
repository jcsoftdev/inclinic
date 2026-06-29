package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/** Archivo seleccionado por el usuario, listo para subirse vía multipart. */
data class PickedFile(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/** Lanzador de la selección de archivo/imagen. Invocar [launch] dispara el picker nativo. */
interface FilePickerLauncher {
    fun launch()
}

/**
 * Recuerda un picker de imágenes/archivos nativo por plataforma.
 * El callback [onPicked] recibe el archivo elegido, o null si el usuario canceló.
 */
@Composable
expect fun rememberFilePicker(onPicked: (PickedFile?) -> Unit): FilePickerLauncher
