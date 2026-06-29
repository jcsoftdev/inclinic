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
 * REQ-4-008
 */
class LogTelemetryService : TelemetryService {
    override fun track(event: String, props: Map<String, Any>) {
        val propsStr = if (props.isEmpty()) "" else " $props"
        println("[Telemetry] $event$propsStr")
    }
}
