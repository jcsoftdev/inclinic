package com.inclinic.app.core.platform

/**
 * HTML self-contained que renderiza un mapa Leaflet con un pin arrastrable.
 *
 * Carga Leaflet desde unpkg y las tiles de OpenStreetMap, así que el WebView necesita
 * internet. El puente nativo→JS expone `window.inclinicSetMarker(lat, lng)`; el puente
 * JS→nativo llama a un objeto inyectado con nombre [BRIDGE_NAME] y método `markerMoved`,
 * enviando un JSON `{"lat":..,"lng":..}` cada vez que el usuario mueve el pin o toca el mapa.
 *
 * [initialLat]/[initialLng]/[initialZoom] fijan la vista inicial.
 */
object LeafletMapHtml {
    /** Nombre del objeto/handler que ambas plataformas inyectan para recibir eventos del pin. */
    const val BRIDGE_NAME = "InclinicMapBridge"

    fun build(initialLat: Double, initialLng: Double, initialZoom: Int = 16): String = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <style>
    html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; }
  </style>
</head>
<body>
  <div id="map"></div>
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <script>
    var map = L.map('map').setView([$initialLat, $initialLng], $initialZoom);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap'
    }).addTo(map);

    var marker = L.marker([$initialLat, $initialLng], { draggable: true }).addTo(map);

    function notify(lat, lng) {
      var payload = JSON.stringify({ lat: lat, lng: lng });
      try {
        // Android: addJavascriptInterface expone un objeto con método markerMoved.
        if (window.$BRIDGE_NAME && window.$BRIDGE_NAME.markerMoved) {
          window.$BRIDGE_NAME.markerMoved(payload);
          return;
        }
      } catch (e) {}
      try {
        // iOS: WKScriptMessageHandler registrado con name = BRIDGE_NAME.
        if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.$BRIDGE_NAME) {
          window.webkit.messageHandlers.$BRIDGE_NAME.postMessage(payload);
        }
      } catch (e) {}
    }

    marker.on('dragend', function (e) {
      var p = marker.getLatLng();
      notify(p.lat, p.lng);
    });

    map.on('click', function (e) {
      marker.setLatLng(e.latlng);
      notify(e.latlng.lat, e.latlng.lng);
    });

    // Puente nativo→JS: reposiciona el pin cuando el texto/coordenadas cambian afuera.
    window.inclinicSetMarker = function (lat, lng) {
      var ll = L.latLng(lat, lng);
      marker.setLatLng(ll);
      map.setView(ll, map.getZoom());
    };
  </script>
</body>
</html>
    """.trimIndent()
}
