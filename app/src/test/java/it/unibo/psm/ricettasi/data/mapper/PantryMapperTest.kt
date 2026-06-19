package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.domain.model.PantryItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * A pantry item goes domain -> DTO when pushed and domain -> entity when stored locally, then back
 * again when read. These round trips check no field gets dropped or swapped (e.g. expiry vs added
 * date).
 */
class PantryMapperTest {

    // Fixed millisecond instants so the conversions are exact (no clock, no sub-millisecond noise).
    private val sample = PantryItem(
        id = "p1",
        ingredientId = "i1",
        quantity = "200g",
        expiryDate = LocalDate.of(2026, 7, 1),
        addedDate = Instant.ofEpochMilli(1_750_000_000_000),
        consumed = true,
        consumedDate = Instant.ofEpochMilli(1_750_100_000_000),
    )

    @Test
    fun domainToEntityAndBack() {
        assertEquals(sample, sample.toEntity().toDomain())
    }

    @Test
    fun domainToDtoAndBack() {
        assertEquals(sample, sample.toDto(userId = "u1").toDomain())
    }
}
