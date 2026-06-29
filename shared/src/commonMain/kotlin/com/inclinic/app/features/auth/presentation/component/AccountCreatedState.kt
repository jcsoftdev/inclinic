package com.inclinic.app.features.auth.presentation.component

/**
 * UI state for the "Cuenta Creada" screen.
 *
 * Minimal: only tracks whether the user already triggered a resend so the
 * screen can give visual feedback on the secondary button.
 */
data class AccountCreatedState(
    val isResent: Boolean = false,
    val isLoading: Boolean = false,
)
