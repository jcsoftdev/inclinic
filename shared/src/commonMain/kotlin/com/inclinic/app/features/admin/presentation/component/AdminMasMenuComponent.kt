package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Hub menu for the Admin "Más" tab.
 *
 * Static list of platform-management entries. Each user tap emits an [Output]
 * which the flow component maps to a masStack push.
 */
interface AdminMasMenuComponent {

    /** Static — no async data needed for a menu. Kept as Value<> for consistency. */
    val state: Value<AdminMasMenuState>

    fun onMenuItemSelected(item: MasMenuItem)
    fun onSettingsClicked()

    sealed interface Output {
        data object NavigateToPatients : Output
        data object NavigateToSpecialties : Output
        data object NavigateToReports : Output
        data object NavigateToReviews : Output
        data object NavigateToBlockedEmails : Output
        data object NavigateToSubscriptions : Output
        data object NavigateToProfile : Output
        data object NavigateToNotifications : Output
        data object NavigateToConfig : Output
        data object NavigateToSecurity : Output
    }
}

data class AdminMasMenuState(
    val items: List<MasMenuItem> = MasMenuItem.all,
)

enum class MasMenuItem(
    val title: String,
    val subtitle: String,
) {
    Patients(
        title    = "Pacientes",
        subtitle = "Buscar y gestionar pacientes",
    ),
    Specialties(
        title    = "Especialidades",
        subtitle = "Catálogo y solicitudes",
    ),
    Reports(
        title    = "Reportes",
        subtitle = "Cola de moderación",
    ),
    Reviews(
        title    = "Reseñas",
        subtitle = "Moderar reseñas de pacientes",
    ),
    BlockedEmails(
        title    = "Emails bloqueados",
        subtitle = "Bloqueos de registro",
    ),
    Subscriptions(
        title    = "Suscripciones",
        subtitle = "Planes de doctores",
    ),
    Profile(
        title    = "Mi perfil",
        subtitle = "Tu cuenta de administrador",
    ),
    Notifications(
        title    = "Notificaciones",
        subtitle = "Alertas de plataforma",
    ),
    Security(
        title    = "Seguridad",
        subtitle = "Verificación en dos pasos (2FA)",
    );

    companion object {
        val all: List<MasMenuItem> = entries
    }
}
