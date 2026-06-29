package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile

interface MiPerfilComponent {
    val state: Value<MiPerfilState>

    fun onRetry()
    fun onNavigateEditSpecialties()
    fun onNavigateRequestSpecialty()
    fun onNavigateMySpecialtyRequests()
    fun onNavigateIncome()
    fun onNavigateReviews()
    fun onNavigatePublicProfile()
    fun onNavigateEditHorarios()
    fun onNavigatePackages()
    fun onNavigateSharing()
    fun onNavigateSettings()
    fun onNavigateTherapyOffers()
    fun onNavigateNoShowQueue()
    fun onLogout()

    sealed interface Output {
        data object EditSpecialties : Output
        data object RequestSpecialty : Output
        data object MySpecialtyRequests : Output
        data object Income : Output
        data object Reviews : Output
        data object PublicProfile : Output
        data object EditHorarios : Output
        data object Packages : Output
        data object Sharing : Output
        data object Settings : Output
        data object TherapyOffers : Output
        data object NoShowQueue : Output
        data object Logout : Output
    }
}

data class MiPerfilState(
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val profile: DoctorProfile? = null,
    val error: String? = null,
)
