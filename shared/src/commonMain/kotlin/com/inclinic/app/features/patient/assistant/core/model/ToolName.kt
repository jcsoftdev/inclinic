package com.inclinic.app.features.patient.assistant.core.model

/**
 * Enumeration of all assistant tools the backend can invoke.
 * [backendName] matches the tool name as emitted in the `tool-input-available` chunk.
 */
enum class ToolName(val backendName: String) {
    LIST_SPECIALTIES("listSpecialties"),
    SEARCH_DOCTORS("searchDoctors"),
    GET_AVAILABILITY("getDoctorAvailability"),
    BOOK_APPOINTMENT("bookAppointment");

    companion object {
        fun fromBackend(name: String): ToolName? = entries.find { it.backendName == name }
    }
}
