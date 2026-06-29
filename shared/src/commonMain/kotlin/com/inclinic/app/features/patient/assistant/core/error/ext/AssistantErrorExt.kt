package com.inclinic.app.features.patient.assistant.core.error.ext

import com.inclinic.app.features.patient.assistant.core.error.AssistantError

/**
 * Maps an [AssistantError] to a user-facing Spanish string for display in the ErrorBanner.
 */
fun AssistantError.toUserMessage(): String = when (this) {
    AssistantError.Disabled    -> "Asistente temporalmente no disponible."
    AssistantError.Forbidden   -> "No tienes permisos."
    is AssistantError.RateLimit -> "Demasiadas consultas. Espera ${retryAfterSeconds}s."
    is AssistantError.Validation -> "Mensaje inválido."
    AssistantError.Unauthorized -> "Tu sesión expiró. Inicia sesión nuevamente."
    AssistantError.Network     -> "Sin conexión. Verifica tu internet."
    is AssistantError.Unknown  -> "Error inesperado. Intenta nuevamente."
}
