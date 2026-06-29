package com.inclinic.app.core.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

private val SPANISH_MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

/**
 * Formatea una fecha ISO de nacimiento ("2000-12-23T00:00:00.000Z" o "2000-12-23")
 * a texto legible en español: "23 de diciembre de 2000".
 *
 * Toma solo la parte de fecha (sin aplicar zona horaria — una fecha de nacimiento
 * no debe correrse de día al convertir a la zona local). Devuelve "—" si está vacía;
 * si no se puede parsear, devuelve el valor original como fallback seguro.
 */
fun formatBirthDate(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return "—"
    val date = runCatching { LocalDate.parse(value.take(10)) }.getOrNull() ?: return value
    val month = SPANISH_MONTHS.getOrNull(date.monthNumber - 1) ?: return value
    return "${date.dayOfMonth} de $month de ${date.year}"
}

/**
 * Convierte una fecha ISO ("2000-12-23T..." o "2000-12-23") a millis UTC de medianoche,
 * para alimentar el estado inicial de un DatePicker de Material3. null si no parsea.
 */
fun isoDateToEpochMillis(raw: String?): Long? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    val date = runCatching { LocalDate.parse(value.take(10)) }.getOrNull() ?: return null
    return date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/** Convierte los millis UTC que devuelve el DatePicker a fecha "AAAA-MM-DD". */
fun epochMillisToIsoDate(millis: Long): String =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString()

/** Millis UTC de "ahora" — para impedir seleccionar fechas futuras. */
fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Año UTC actual — cota superior del rango de años del DatePicker. */
fun currentYearUtc(): Int = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
