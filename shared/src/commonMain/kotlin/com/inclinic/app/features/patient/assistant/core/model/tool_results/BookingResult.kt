package com.inclinic.app.features.patient.assistant.core.model.tool_results

/**
 * Domain model for the result of a `bookAppointment` tool call.
 *
 * - [Ok] — appointment created; [paymentRedirectPath] is `/patient/payment/{appointmentId}`.
 *   In v1 this path is shown as non-clickable text (no navigation). v2 wires real nav.
 * - [Failed] — slot taken or other domain error; [errorCode] = "slot_taken" etc.
 */
sealed class BookingResult {
    data class Ok(
        val appointmentId: String,
        val paymentRedirectPath: String,
    ) : BookingResult()

    data class Failed(
        val errorCode: String,
        val message: String,
    ) : BookingResult()
}
