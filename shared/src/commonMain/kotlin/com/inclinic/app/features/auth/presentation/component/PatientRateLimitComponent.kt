package com.inclinic.app.features.auth.presentation.component

/**
 * Presentation contract for the standalone "too many attempts" (HTTP 429) screen.
 *
 * Reached from [LoginComponent] when [com.inclinic.app.features.auth.core.error.AuthError.TooManyAttempts]
 * is returned instead of being shown as an inline banner (previous behavior).
 *
 * No mutable state is needed — this is a static informational screen, same shape as
 * [RegisterChooserComponent].
 */
interface PatientRateLimitComponent {
    /** Called when the user taps "Volver a iniciar sesión". */
    fun onBackToLogin()
}
