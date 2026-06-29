package com.inclinic.app.features.patient.assistant.core.error

/**
 * Sealed hierarchy of all errors the patient assistant feature can surface.
 * Extends [Throwable] for compatibility with [Result]/[runCatching] chains.
 *
 * HTTP → domain mapping (in [com.inclinic.app.features.patient.assistant.infrastructure.KtorAssistantChatDataSource]):
 *   503 ASSISTANT_DISABLED → [Disabled]
 *   403                    → [Forbidden]
 *   429 + Retry-After      → [RateLimit]
 *   422                    → [Validation]
 *   401                    → [Unauthorized] + SessionEvents.expired
 *   network failure        → [Network]
 *   anything else          → [Unknown]
 */
sealed class AssistantError : Throwable() {

    /** 503 — `ASSISTANT_ENABLED=false` on the backend. */
    data object Disabled : AssistantError()

    /** 403 — caller is not a PATIENT or the conversation belongs to another patient. */
    data object Forbidden : AssistantError()

    /** 429 — rate limit exceeded; [retryAfterSeconds] comes from the `Retry-After` header. */
    data class RateLimit(val retryAfterSeconds: Int) : AssistantError()

    /** 422 — request body failed Zod validation; [field] may identify the specific field. */
    data class Validation(val field: String?) : AssistantError()

    /** 401 — JWT expired or missing. Triggers [SessionEvents.expired] to redirect to login. */
    data object Unauthorized : AssistantError()

    /** No network connectivity or request timed out. */
    data object Network : AssistantError()

    /** Catch-all; [original] may be null when mapping from a string error code. */
    data class Unknown(val original: Throwable?) : AssistantError()
}
