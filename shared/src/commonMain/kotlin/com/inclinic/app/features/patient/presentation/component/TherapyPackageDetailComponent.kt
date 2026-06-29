package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.TherapyPackage

interface TherapyPackageDetailComponent {
    val state: Value<TherapyPackageDetailState>

    fun onTabChange(tab: SessionsTab)
    fun onScheduleNextSession()
    fun onBack()
    fun onErrorDismissed()

    sealed interface Output {
        data class NavigateToScheduleSession(val packageId: String, val doctorId: String) : Output
        data object Back : Output
    }
}

enum class SessionsTab { UPCOMING, HISTORY }

data class TherapyPackageDetailState(
    val selectedTab: SessionsTab = SessionsTab.UPCOMING,
    val therapyPackage: TherapyPackage? = null,
    val sessions: List<PackageSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
