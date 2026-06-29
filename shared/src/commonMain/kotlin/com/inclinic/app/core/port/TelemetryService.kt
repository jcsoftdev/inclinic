package com.inclinic.app.core.port

/**
 * Pluggable telemetry sink.
 *
 * The interface is intentionally thin so it can be backed by:
 *  - [LogTelemetryService] (debug / initial production — no external dependency)
 *  - A Firebase Analytics / Amplitude / Mixpanel bridge added later without
 *    changing any call sites.
 *
 * REQ-4-008
 */
interface TelemetryService {
    /**
     * Record a named event with optional properties.
     *
     * @param event  Snake-case event name (e.g. "screen_view", "login_success").
     * @param props  Flat map of string-keyed primitives. Implementations must
     *               handle at least [String], [Int], [Long], [Double], [Boolean].
     */
    fun track(event: String, props: Map<String, Any> = emptyMap())
}
