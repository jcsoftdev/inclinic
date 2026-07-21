package com.inclinic.app.core.telemetry

import com.inclinic.app.core.port.TelemetryService

/**
 * Stdout-only telemetry implementation.
 *
 * Produces a `[Telemetry]` prefixed line per event so events are visible in
 * Logcat (Android) and the Xcode console (iOS) without any external SDK.
 *
 * This implementation is intended to be the default until a real analytics
 * provider is integrated. Swap it out in [CoreModule] when that time comes.
 *
 * [enabled] gates emission: in PROD it is `false`, so nothing is written to the
 * device log — preventing any event (and future props) from leaking to Logcat.
 *
 * REQ-4-008
 */
class LogTelemetryService(private val enabled: Boolean = true) : TelemetryService {
    override fun track(event: String, props: Map<String, Any>) {
        if (!enabled) return
        val propsStr = if (props.isEmpty()) "" else " $props"
        println("[Telemetry] $event$propsStr")
    }
}
