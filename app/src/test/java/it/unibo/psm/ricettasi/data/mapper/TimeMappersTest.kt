package it.unibo.psm.ricettasi.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Dates cross the network as text and Postgres can return a timestamptz either with a "Z" or with
 * an explicit "+hh:mm" offset. Sync mapping relies on both forms parsing to the right instant, so
 * a wrong/ignored offset would shift every synced date.
 */
class TimeMappersTest {

    @Test
    fun zAndOffsetMeanTheSameInstant() {
        // 12:15 at +02:00 is the same moment as 10:15 at UTC.
        val utc = "2026-06-25T10:15:30Z".toInstant()
        val plusTwo = "2026-06-25T12:15:30+02:00".toInstant()
        assertEquals(utc, plusTwo)
    }

    @Test
    fun parsesIsoDate() {
        assertEquals(LocalDate.of(2026, 6, 25), "2026-06-25".toLocalDate())
    }
}
