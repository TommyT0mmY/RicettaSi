package it.unibo.psm.ricettasi.data.mapper

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/**
 * Converts an ISO `timestamptz` returned by Postgres to [Instant], tolerating both an explicit
 * offset ("...+00:00") and the 'Z' form.
 */
internal fun String.toInstant(): Instant =
    try {
        OffsetDateTime.parse(this).toInstant()
    } catch (_: DateTimeParseException) {
        Instant.parse(this)
    }

/** Converts an ISO `date` (`yyyy-MM-dd`) to [LocalDate]. */
internal fun String.toLocalDate(): LocalDate = LocalDate.parse(this)
