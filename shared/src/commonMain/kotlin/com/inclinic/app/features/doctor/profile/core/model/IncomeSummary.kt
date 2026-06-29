package com.inclinic.app.features.doctor.profile.core.model

/**
 * Doctor's income summary from GET /api/doctors/me/metrics (monthRevenue section).
 *
 * Note: The backend does NOT provide time-series bar data -- only aggregate
 * month-level figures. The bars list is always empty in the default
 * implementation; it will remain empty until a dedicated time-series endpoint
 * exists on the backend.
 */
data class IncomeSummary(
    val totalCents: Long,
    val commissionCents: Long,
    val netCents: Long,
    val sessions: Int,
    val growthPct: Double?,
    val availableCents: Long = 0L,
    val bars: List<IncomeBar> = emptyList(),
)

data class IncomeBar(
    val label: String,
    val amountCents: Long,
)
