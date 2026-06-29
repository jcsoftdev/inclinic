package com.inclinic.app.features.patient.assistant.presentation.ui

import com.inclinic.app.features.patient.assistant.core.model.tool_results.AvailabilitySlot
import com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult
import com.inclinic.app.features.patient.assistant.core.model.tool_results.DoctorResult
import com.inclinic.app.features.patient.assistant.infrastructure.dto.AvailabilitySlotDto
import com.inclinic.app.features.patient.assistant.infrastructure.dto.BookingResultDto
import com.inclinic.app.features.patient.assistant.infrastructure.dto.DoctorResultDto
import com.inclinic.app.features.patient.assistant.infrastructure.dto.bookingResultDtoToDomain
import com.inclinic.app.features.patient.assistant.infrastructure.dto.toDomain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

private val lenientJson = Json { ignoreUnknownKeys = true }

/**
 * Parses the raw [JsonElement] from a `searchDoctors` tool-result into a list of [DoctorResult].
 * Returns an empty list on any parse error so the UI degrades gracefully.
 */
fun parseDoctorList(result: JsonElement): List<DoctorResult> = runCatching {
    lenientJson.decodeFromJsonElement<List<DoctorResultDto>>(result).map { it.toDomain() }
}.getOrElse { emptyList() }

/**
 * Parses the raw [JsonElement] from a `getDoctorAvailability` tool-result.
 * Returns a [Pair] of (date string, list of [AvailabilitySlot]).
 * On parse error returns ("", emptyList()).
 */
fun parseAvailability(result: JsonElement): Pair<String, List<AvailabilitySlot>> = runCatching {
    val obj = result as? kotlinx.serialization.json.JsonObject ?: return@runCatching ("" to emptyList())
    val date = obj["date"]?.let {
        lenientJson.decodeFromJsonElement<String>(it)
    } ?: ""
    val slots = obj["slots"]?.let {
        lenientJson.decodeFromJsonElement<List<AvailabilitySlotDto>>(it).map { dto -> dto.toDomain() }
    } ?: emptyList()
    date to slots
}.getOrElse { "" to emptyList() }

/**
 * Parses the raw [JsonElement] from a `bookAppointment` tool-result into a [BookingResult].
 * Returns [BookingResult.Failed] with a generic message on any parse error.
 */
fun parseBookingResult(result: JsonElement): BookingResult = runCatching {
    val dto = lenientJson.decodeFromJsonElement<BookingResultDto>(result)
    bookingResultDtoToDomain(dto)
}.getOrElse {
    BookingResult.Failed(
        errorCode = "PARSE_ERROR",
        message = "No se pudo leer el resultado de la reserva.",
    )
}
