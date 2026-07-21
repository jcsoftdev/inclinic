package com.inclinic.app.core.platform

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun LeafletMapView(
    center: MapCoordinate,
    onMarkerMoved: (MapCoordinate) -> Unit,
    modifier: Modifier,
) {
    // rememberUpdatedState: el bridge (creado una vez) siempre ve el callback actual.
    val currentOnMoved = rememberUpdatedState(onMarkerMoved)
    val bridge = remember {
        object {
            @JavascriptInterface
            fun markerMoved(payload: String) {
                val json = runCatching { JSONObject(payload) }.getOrNull() ?: return
                val lat = json.optDouble("lat", Double.NaN)
                val lng = json.optDouble("lng", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    currentOnMoved.value(MapCoordinate(lat, lng))
                }
            }
        }
    }

    // La vista inicial usa la primera coordenada; los cambios posteriores se aplican vía JS.
    val initial = remember { center }
    var loaded = remember { booleanArrayOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(bridge, LeafletMapHtml.BRIDGE_NAME)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        loaded[0] = true
                    }
                }
                loadDataWithBaseURL(
                    "https://tile.openstreetmap.org",
                    LeafletMapHtml.build(initial.lat, initial.lng),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
        update = { webView ->
            if (loaded[0]) {
                webView.evaluateJavascript(
                    "window.inclinicSetMarker(${center.lat}, ${center.lng});",
                    null,
                )
            }
        },
    )
}
