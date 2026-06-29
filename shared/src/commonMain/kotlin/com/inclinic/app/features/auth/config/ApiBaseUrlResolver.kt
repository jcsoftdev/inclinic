package com.inclinic.app.features.auth.config

/**
 * Platform-specific loopback host for reaching the dev machine from a local runtime.
 *
 * - Android emulator: `10.0.2.2` is a special alias mapped to the host's loopback.
 * - iOS simulator: shares the host network, so `localhost` reaches the host directly.
 *
 * Used only to rewrite DEV loopback URLs (see [resolveApiBaseUrl]). Staging/prod use
 * real domains and are never rewritten.
 */
expect val platformLoopbackHost: String

private val LOOPBACK_HOSTS = setOf("localhost", "127.0.0.1", "10.0.2.2")

/**
 * In DEV, rewrites a loopback host to the platform-specific loopback host so the same
 * config works on Android emulator and iOS simulator without manual edits.
 *
 * Only known loopback hosts ([LOOPBACK_HOSTS]) are rewritten — a real LAN IP or domain
 * (e.g. a physical device pointing at `192.168.x.x`) is preserved untouched.
 * Non-DEV environments are never rewritten.
 *
 * Scheme, port, and path are preserved.
 */
fun resolveApiBaseUrl(rawUrl: String, rawEnv: String, loopbackHost: String): String {
    if (rawEnv != Environment.DEV.name) return rawUrl

    val schemeSep = rawUrl.indexOf("://")
    if (schemeSep < 0) return rawUrl

    val scheme = rawUrl.substring(0, schemeSep)
    val rest = rawUrl.substring(schemeSep + 3)

    val authorityEnd = rest.indexOf('/').let { if (it < 0) rest.length else it }
    val authority = rest.substring(0, authorityEnd)
    val path = rest.substring(authorityEnd)

    val portSep = authority.indexOf(':')
    val host = if (portSep < 0) authority else authority.substring(0, portSep)
    val port = if (portSep < 0) "" else authority.substring(portSep)

    if (host !in LOOPBACK_HOSTS) return rawUrl

    return "$scheme://$loopbackHost$port$path"
}
