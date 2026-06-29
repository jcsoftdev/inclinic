package com.inclinic.app.features.doctor.settings.presentation.component

import com.arkivanov.decompose.value.Value

interface DoctorSettingsComponent {
    val state: Value<DoctorSettingsState>

    fun onBack()
    fun onLogOut()
    fun onDeleteAccount()
    fun onToggleNewAppointments(enabled: Boolean)
    fun onToggleChatMessages(enabled: Boolean)
    fun onToggleAppointmentReminders(enabled: Boolean)
    fun onToggleTwoFactor(enabled: Boolean)

    // ── MercadoPago connect/disconnect ────────────────────────────────────────

    /**
     * Initiates the MercadoPago OAuth flow.
     * On success, sets [DoctorSettingsState.mercadoPagoConnectUrl] so the screen can open it.
     * On 503 MP_NOT_CONFIGURED, sets [DoctorSettingsState.mercadoPagoError].
     */
    fun onConnectMercadoPago()

    /**
     * Called by the screen after it has opened the OAuth URL in the browser.
     * Optimistically marks the integration as connected and clears the URL from state.
     */
    fun onMercadoPagoConnectUrlConsumed()

    /**
     * Disconnects the MercadoPago integration.
     * On success, sets [DoctorSettingsState.mercadoPagoConnected] to false.
     */
    fun onDisconnectMercadoPago()

    sealed interface Output {
        data object Back : Output
        data object LoggedOut : Output
        data object NavigateToDeleteAccount : Output
    }
}

data class DoctorSettingsState(
    val newAppointmentsEnabled: Boolean = true,
    val chatMessagesEnabled: Boolean = true,
    val appointmentRemindersEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val isLoggingOut: Boolean = false,

    // ── MercadoPago ──────────────────────────────────────────────────────────
    /** Whether the doctor currently has MercadoPago connected (optimistic). */
    val mercadoPagoConnected: Boolean = false,
    /** True while a connect or disconnect request is in-flight. */
    val isMercadoPagoLoading: Boolean = false,
    /** Non-null when a connect/disconnect error occurred. */
    val mercadoPagoError: String? = null,
    /**
     * The OAuth URL to open in the browser. Non-null only between the moment
     * the URL is fetched and when the screen consumes it via [DoctorSettingsComponent.onMercadoPagoConnectUrlConsumed].
     */
    val mercadoPagoConnectUrl: String? = null,
)
