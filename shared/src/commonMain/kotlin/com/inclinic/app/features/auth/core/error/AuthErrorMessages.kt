package com.inclinic.app.features.auth.core.error

/**
 * Shown at Login after a real 401/token-expiry ([com.inclinic.app.core.events.SessionExpiryReason.EXPIRED]).
 * Never shown for an explicit, user-initiated logout — see [com.inclinic.app.core.navigation.PendingSessionMessage].
 * Single source of truth, also used by [AuthError.Unauthorized]'s inline banner copy.
 */
const val SessionExpiredMessage = "Tu sesión expiró, ingresa nuevamente."

/**
 * Maps [AuthError] sealed variants to user-visible Spanish strings.
 *
 * Returns null for variants that are either absent (null receiver) or handled
 * inline at the field level ([AuthError.ValidationError]).
 *
 * This mapping was previously embedded in the old `ErrorBanner` composable and
 * has been lifted here so it remains accessible from any presentation layer
 * without coupling to a specific Compose component.
 */
fun AuthError?.toUserMessage(): String? = when (this) {
    null                                  -> null
    is AuthError.ValidationError          -> null  // surfaced inline per field
    is AuthError.FreelanceValidationError -> message // carries a user-ready message
    is AuthError.Unauthorized             -> SessionExpiredMessage
    is AuthError.InvalidCredentials       -> "Email o contraseña incorrectos."
    is AuthError.InactiveAccount          -> "Tu cuenta no está activada. Revisa tu correo."
    is AuthError.SuspendedAccount         -> "Tu cuenta está suspendida. Contacta soporte."
    is AuthError.TooManyAttempts          -> "Demasiados intentos. Intenta de nuevo en unos minutos."
    is AuthError.EmailAlreadyExists       -> "Este email ya está registrado."
    is AuthError.InvalidToken             -> "El código o enlace no es válido o ha expirado."
    is AuthError.NetworkError             -> "Sin conexión. Verifica tu internet."
    is AuthError.MalformedResponse        -> "Error inesperado. Intenta nuevamente."
    is AuthError.ServerError              -> "Error del servidor ($status). Intenta más tarde."
    is AuthError.Unknown                  -> "Error inesperado. Intenta nuevamente."
}
