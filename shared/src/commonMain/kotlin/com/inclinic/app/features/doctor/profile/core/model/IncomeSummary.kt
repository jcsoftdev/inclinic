package com.inclinic.app.features.doctor.profile.core.model

/**
 * Breakdown of the month's revenue by payment-hold state.
 * Populated from monthRevenue.breakdown when the backend includes it.
 * All values are in soles (not cents).
 */
data class IncomeBreakdown(
    val retainedCents: Long,
    val releasedCents: Long,
    val refundedCents: Long,
)

/**
 * Doctor's income summary from GET /api/doctors/me/metrics (monthRevenue section).
 *
 * Note: The backend does NOT provide time-series bar data -- only aggregate
 * month-level figures. The bars list is always empty in the default
 * implementation; it will remain empty until a dedicated time-series endpoint
 * exists on the backend.
 *
 * [breakdown] is null when the backend does not include the breakdown object
 * (backward compatibility). UI must handle null gracefully with an empty state.
 */
data class IncomeSummary(
    val totalCents: Long,
    val commissionCents: Long,
    val netCents: Long,
    val sessions: Int,
    val growthPct: Double?,
    val availableCents: Long = 0L,
    val bars: List<IncomeBar> = emptyList(),
    val breakdown: IncomeBreakdown? = null,
)

data class IncomeBar(
    val label: String,
    val amountCents: Long,
)
