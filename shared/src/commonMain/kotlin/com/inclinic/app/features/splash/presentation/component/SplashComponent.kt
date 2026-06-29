package com.inclinic.app.features.splash.presentation.component

interface SplashComponent {
    sealed interface Output {
        data object NavigateToAuth : Output
        data class NavigateToPatient(val patientId: String) : Output
        data class NavigateToDoctor(val doctorId: String) : Output
        data class NavigateToAdmin(val adminId: String) : Output
    }
}
