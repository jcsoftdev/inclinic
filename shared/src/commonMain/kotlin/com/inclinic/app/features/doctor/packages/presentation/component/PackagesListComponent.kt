package com.inclinic.app.features.doctor.packages.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.packages.core.model.PackageStatus
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage

interface PackagesListComponent {
    val state: Value<PackagesListState>

    fun onRefresh()
    fun onCreateClicked()
    fun onPackageClicked(id: String)
    fun onTabSelected(tab: PackageListTab)
    fun onBack()

    sealed interface Output {
        data object NavigateToCreate : Output
        data class NavigateToDetail(val packageId: String) : Output
        data object Back : Output
    }
}

/** Top filter tabs in the "Mis Paquetes" screen. */
enum class PackageListTab(val label: String) {
    ACTIVE("Activos"),
    PENDING("Pendientes"),
    ARCHIVED("Archivados"),
    ;

    fun matches(status: PackageStatus): Boolean = when (this) {
        ACTIVE -> status == PackageStatus.ACTIVE
        PENDING -> status == PackageStatus.PENDING_PAYMENT
        ARCHIVED -> status == PackageStatus.CANCELLED || status == PackageStatus.COMPLETED
    }
}

data class PackagesListState(
    val packages: List<TherapyPackage> = emptyList(),
    val selectedTab: PackageListTab = PackageListTab.ACTIVE,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    /** Packages visible under the currently selected tab. */
    val visiblePackages: List<TherapyPackage>
        get() = packages.filter { selectedTab.matches(it.status) }
}
