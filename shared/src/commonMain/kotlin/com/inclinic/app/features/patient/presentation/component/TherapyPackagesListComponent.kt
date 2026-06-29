package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.TherapyPackage

interface TherapyPackagesListComponent {
    val state: Value<TherapyPackagesListState>

    fun onTabChange(tab: PackagesTab)
    fun onPackageTapped(packageId: String)
    fun onBuyPackage()
    fun onBack()
    fun onErrorDismissed()

    sealed interface Output {
        data class NavigateToPackageDetail(val packageId: String) : Output
        data object NavigateToOffers : Output
        data object Back : Output
    }
}

enum class PackagesTab { ACTIVE, PENDING_PAYMENT, HISTORY }

data class TherapyPackagesListState(
    val selectedTab: PackagesTab = PackagesTab.ACTIVE,
    val packages: List<TherapyPackage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
