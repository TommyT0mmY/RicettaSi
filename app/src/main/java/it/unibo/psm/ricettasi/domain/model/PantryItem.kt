package it.unibo.psm.ricettasi.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * How urgent an item's expiry looks when shown in the pantry list. This only drives the little
 * colored label on screen, it does not affect recipe availability at all. Whether you "have"
 * an ingredient is a yes/no thing regardless of how close it is to expiring. Kept those two
 * things separate on purpose, otherwise a recipe could keep popping in and out of a list just
 * because the milk expires tomorrow, which would feel buggy.
 *
 * The day ranges below line up with what's actually printed on screen: today, tomorrow or
 * already past due counts as CRITICAL, 2-3 days out is WARNING, up to a week is SECONDARY, and
 * anything further away is just OK.
 */
enum class ExpiryStatus {
    CRITICAL,
    WARNING,
    SECONDARY,
    OK;

    companion object {
        // null means no expiry date was set at all (flour, salt, things that don't really go bad)
        fun fromExpiry(expiryDate: LocalDate?, today: LocalDate = LocalDate.now()): ExpiryStatus? {
            if (expiryDate == null) return null
            val days = ChronoUnit.DAYS.between(today, expiryDate)
            return when {
                days <= 1 -> CRITICAL
                days <= 3 -> WARNING
                days <= 7 -> SECONDARY
                else -> OK
            }
        }
    }
}

/**
 * A single item the user put in their pantry. Buying milk twice means two separate
 * PantryItem entries with the same [ingredientId]. They're never merged into one, since
 * each purchase can have its own expiry date and merging would just throw that away.
 *
 * [quantity] and [expiryDate] are both optional and only there to inform the user (e.g. "200g",
 * a date shown as "scade venerdì"). Nothing in the availability/matching logic reads them.
 */
data class PantryItem(
    val id: String,
    val ingredientId: String,
    val quantity: String? = null,
    val expiryDate: LocalDate? = null,
    val addedDate: Instant,
    // false while still active in the pantry, true once moved to "Finished items"
    val consumed: Boolean = false,
    val consumedDate: Instant? = null,
) {
    fun expiryStatus(today: LocalDate = LocalDate.now()): ExpiryStatus? =
        ExpiryStatus.fromExpiry(expiryDate, today)
}
