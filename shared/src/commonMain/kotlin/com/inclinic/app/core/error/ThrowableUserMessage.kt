package com.inclinic.app.core.error

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException

/**
 * Maps ANY [Throwable] to a safe, user-facing Spanish message.
 *
 * Why this exists: presentation components used to push `throwable.message` straight
 * into UI state. On iOS the Ktor Darwin engine packs the entire `NSError` description
 * into that message (e.g. `Exception in http request: Error Domain=NSURLErrorDomain
 * Code=-1004 "Could not connect to the server." ...`), so the raw dump rendered on
 * screen. This function NEVER leaks an engine/exception string to the user.
 *
 * Contract:
 * - Connection / timeout / network failures ALWAYS win and return a friendly,
 *   actionable message — these are the ones that previously leaked.
 * - Typed HTTP errors map to a short status-aware message.
 * - Anything else falls back to [fallback] (the caller's context-specific copy,
 *   e.g. "Error al cargar paquetes") when provided, otherwise a generic message.
 *
 * @param fallback context-specific copy to use for non-network, non-HTTP errors.
 */
fun Throwable.toUserMessage(fallback: String? = null): String = when (this) {
    is ApiError                    -> apiErrorMessage(fallback)
    is HttpRequestTimeoutException -> CONNECTION_MESSAGE
    is IOException                 -> CONNECTION_MESSAGE
    is ClientRequestException      -> clientErrorMessage()
    is ServerResponseException     -> SERVER_MESSAGE
    is ResponseException           -> SERVER_MESSAGE
    // Domain validation errors carry intentional user-facing messages — surface them as-is
    is IllegalArgumentException    -> message ?: (fallback ?: GENERIC_MESSAGE)
    else                           -> if (looksLikeConnectionFailure()) CONNECTION_MESSAGE
                                      else fallback ?: GENERIC_MESSAGE
}

private const val CONNECTION_MESSAGE =
    "No se pudo conectar al servidor. Verifica tu conexión e intenta de nuevo."
private const val SERVER_MESSAGE =
    "El servidor no está disponible en este momento. Intenta más tarde."
private const val GENERIC_MESSAGE =
    "Ocurrió un error inesperado. Intenta de nuevo."

private fun ApiError.apiErrorMessage(fallback: String?): String = when (this) {
    ApiError.Network        -> CONNECTION_MESSAGE
    ApiError.Timeout        -> CONNECTION_MESSAGE
    ApiError.Unauthorized   -> "Tu sesión expiró. Inicia sesión nuevamente."
    ApiError.Forbidden      -> "No tienes permisos para esta acción."
    ApiError.NotFound       -> fallback ?: "No encontramos lo que buscabas."
    is ApiError.Conflict    -> message ?: fallback ?: GENERIC_MESSAGE
    is ApiError.BadRequest  -> message ?: fallback ?: GENERIC_MESSAGE
    is ApiError.Server      -> SERVER_MESSAGE
}

private fun ClientRequestException.clientErrorMessage(): String = when (response.status) {
    HttpStatusCode.Unauthorized -> "Tu sesión expiró. Inicia sesión nuevamente."
    HttpStatusCode.Forbidden    -> "No tienes permisos para esta acción."
    HttpStatusCode.NotFound     -> "No encontramos lo que buscabas."
    else                        -> GENERIC_MESSAGE
}

/**
 * Heuristic for connection failures that surface as a plain [Exception] on
 * Kotlin/Native (no typed Ktor/IO exception), where the engine message carries
 * the NSError/CFNetwork dump.
 */
private fun Throwable.looksLikeConnectionFailure(): Boolean {
    val msg = message ?: return false
    return CONNECTION_MARKERS.any { msg.contains(it, ignoreCase = true) }
}

private val CONNECTION_MARKERS = listOf(
    "Could not connect to the server",
    "NSURLErrorDomain",
    "kCFErrorDomainCFNetwork",
    "Connection refused",
    "Network is unreachable",
    "The Internet connection appears to be offline",
    "timed out",
    "Failed to connect",
    "Unable to resolve host",
    "No address associated with hostname",
)
