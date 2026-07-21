package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * Mapa Leaflet en iOS sobre WKWebView.
 *
 * NOTA: compila, pero el interop WebKit no se valida en CI; probar en device/simulador.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LeafletMapView(
    center: MapCoordinate,
    onMarkerMoved: (MapCoordinate) -> Unit,
    modifier: Modifier,
) {
    val currentOnMoved = rememberUpdatedState(onMarkerMoved)
    val handler = remember {
        MapMessageHandler { lat, lng -> currentOnMoved.value(MapCoordinate(lat, lng)) }
    }
    val initial = remember { center }

    // Se guarda la referencia al WebView para poder reposicionar el pin vía JS en updates.
    val webViewHolder = remember { arrayOfNulls<WKWebView>(1) }

    UIKitView(
        modifier = modifier,
        factory = {
            val contentController = WKUserContentController()
            contentController.addScriptMessageHandler(handler, LeafletMapHtml.BRIDGE_NAME)
            val config = WKWebViewConfiguration().apply {
                userContentController = contentController
            }
            val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
            webView.loadHTMLString(
                LeafletMapHtml.build(initial.lat, initial.lng),
                baseURL = NSURL(string = "https://tile.openstreetmap.org"),
            )
            webViewHolder[0] = webView
            webView
        },
        update = { _ ->
            webViewHolder[0]?.evaluateJavaScript(
                "window.inclinicSetMarker(${center.lat}, ${center.lng});",
                completionHandler = null,
            )
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class MapMessageHandler(
    private val onMoved: (Double, Double) -> Unit,
) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val body = didReceiveScriptMessage.body as? String ?: return
        val (lat, lng) = parseLatLng(body) ?: return
        onMoved(lat, lng)
    }
}

/** Parseo minimalista de `{"lat":..,"lng":..}` para no depender de un JSON runtime en iOS. */
private fun parseLatLng(json: String): Pair<Double, Double>? {
    val lat = extractNumber(json, "lat") ?: return null
    val lng = extractNumber(json, "lng") ?: return null
    return lat to lng
}

private fun extractNumber(json: String, key: String): Double? {
    val marker = "\"$key\""
    val keyIdx = json.indexOf(marker)
    if (keyIdx < 0) return null
    val colon = json.indexOf(':', keyIdx + marker.length)
    if (colon < 0) return null
    var i = colon + 1
    while (i < json.length && json[i].isWhitespace()) i++
    val start = i
    while (i < json.length && (json[i].isDigit() || json[i] == '.' || json[i] == '-' || json[i] == '+' || json[i] == 'e' || json[i] == 'E')) i++
    return json.substring(start, i).toDoubleOrNull()
}
