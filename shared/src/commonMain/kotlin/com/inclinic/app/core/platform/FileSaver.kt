package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/**
 * Saves arbitrary byte content to a user-accessible location using the platform's
 * native file-save / share mechanism:
 *
 *  - Android: MediaStore Downloads (API 29+, matches minSdk=29)
 *  - iOS: NSTemporaryDirectory + UIActivityViewController share sheet
 */
interface FileSaver {
    /**
     * Saves [bytes] to a file named [fileName] with the given [mimeType].
     * On Android the file lands in the public Downloads folder.
     * On iOS a share sheet is presented so the user can choose where to save.
     */
    fun saveBytes(fileName: String, mimeType: String, bytes: ByteArray)
}

/**
 * Returns a platform-specific [FileSaver] remembered across recompositions.
 * Follow the same pattern as [rememberPdfOpener].
 */
@Composable
expect fun rememberFileSaver(): FileSaver
