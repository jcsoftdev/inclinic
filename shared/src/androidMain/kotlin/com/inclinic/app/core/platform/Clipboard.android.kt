package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
actual fun rememberClipboard(): Clipboard {
    val manager = LocalClipboardManager.current
    return remember(manager) {
        object : Clipboard {
            override fun copy(text: String, label: String?) {
                manager.setText(AnnotatedString(text))
            }
        }
    }
}
