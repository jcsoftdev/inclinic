package com.inclinic.app.features.patient.assistant.core.model.tool_results

/**
 * Domain model for a single doctor returned by the `searchDoctors` tool.
 *
 * Fields mirror the backend tool output from [searchDoctorsTool.execute]:
 * id, name, bio (trimmed to 150 chars), consultationPrice (PEN), ratingAvg, ratingCount.
 */
data class DoctorResult(
    val id: String,
    val name: String,
    val bio: String,
    val consultationPrice: Double,
    val ratingAvg: Double?,
    val ratingCount: Int,
)
