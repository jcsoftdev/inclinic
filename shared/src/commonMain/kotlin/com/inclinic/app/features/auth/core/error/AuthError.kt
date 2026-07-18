package com.inclinic.app.features.auth.core.error

/**
 * Sealed hierarchy of all errors the auth feature can surface.
 * Extends Throwable so it integrates cleanly with Result<T> / runCatching.
 */
sealed class AuthError : Throwable() {

    /** Email/password pair does not match any account, or 401 with no special code. */
    data object InvalidCredentials : AuthError()

    /** Account exists but is inactive (email unverified / disabled). 401 + code=INACTIVE. */
    data object InactiveAccount : AuthError()

    /** Account is administratively suspended. 403 + code=ACCOUNT_SUSPENDED. */
    data object SuspendedAccount : AuthError()

    /** Too many requests in a short window (login/register/forgot-password rate limit). 429. */
    data object TooManyAttempts : AuthError()

    /** No connectivity or request timed out. No tokens must be stored on this error. */
    data object NetworkError : AuthError()

    /** Response body did not conform to the expected shape (parse/serialization failure). */
    data object MalformedResponse : AuthError()

    /** Backend returned a 5xx status code. */
    data class ServerError(val status: Int) : AuthError()

    /** 401 after token refresh — session is definitively expired. */
    data object Unauthorized : AuthError()

    /** Account conflict — email already registered (409). */
    data object EmailAlreadyExists : AuthError()

    /** Token/code invalid or expired during activate/reset. */
    data object InvalidToken : AuthError()

    /** Input validation failed before any network call was made. */
    data class ValidationError(val field: Field, val kind: Kind) : AuthError() {
        enum class Field { NAME, LAST_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD }
        enum class Kind {
            EMPTY_NAME,
            EMPTY_LAST_NAME,
            INVALID_EMAIL,
            EMPTY_PASSWORD,
            WEAK_PASSWORD,
            PASSWORD_MISMATCH,
        }
    }

    /**
     * Validation error for the freelance doctor registration form.
     * Covers fields not in [ValidationError] (phone, documents, specialtyIds,
     * consultationPrice, schedules).
     */
    data class FreelanceValidationError(
        val field: Field,
        override val message: String,
    ) : AuthError() {
        enum class Field { PHONE, DOCUMENTS, SPECIALTY_IDS, CONSULTATION_PRICE, SCHEDULES }
    }

    /** Catch-all for errors that don't map to any known category. */
    data class Unknown(val original: Throwable) : AuthError()
}
