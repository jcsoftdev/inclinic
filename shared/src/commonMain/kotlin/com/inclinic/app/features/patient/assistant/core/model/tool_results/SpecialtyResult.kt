package com.inclinic.app.features.patient.assistant.core.model.tool_results

/**
 * Domain model for a specialty returned by the `listSpecialties` tool.
 * This tool result is consumed silently (no UI card rendered per spec).
 */
data class SpecialtyResult(
    val id: String,
    val name: String,
    val description: String,
    val icon: String?,
)
