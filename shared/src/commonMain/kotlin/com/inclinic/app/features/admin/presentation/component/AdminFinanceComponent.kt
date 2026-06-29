package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminTopDoctor

interface AdminFinanceComponent {
    val state: Value<AdminFinanceState>

    fun onRefresh()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminFinanceState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Hero card
    val balanceTotal: String = "S/ 0",
    val released: String = "S/ 0",
    val held: String = "S/ 0",

    // Metric tiles
    val thisMonthRevenue: String = "S/ 0",
    val heldAmount: String = "S/ 0",
    val heldCount: Int = 0,

    // Movimientos
    val topDoctors: List<AdminTopDoctor> = emptyList(),
)
