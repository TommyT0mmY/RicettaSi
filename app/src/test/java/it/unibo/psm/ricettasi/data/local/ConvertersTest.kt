package it.unibo.psm.ricettasi.data.local

import it.unibo.psm.ricettasi.data.remote.dto.CookedRecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.FavoriteDto
import it.unibo.psm.ricettasi.data.remote.dto.IngredientDto
import it.unibo.psm.ricettasi.data.remote.dto.PantryItemDto
import it.unibo.psm.ricettasi.data.remote.dto.SyncPayload
import it.unibo.psm.ricettasi.data.sync.SyncAction
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The offline queue stores each pending write as the JSON of its DTO, through these converters.
 * It only works if a DTO encoded as a SyncPayload decodes back to the exact same DTO: the
 * serializer writes a hidden "type" field so it knows which subtype to rebuild.
 */
class ConvertersTest {

    private fun roundTrip(payload: SyncPayload): SyncPayload =
        Converters.toSyncPayload(Converters.fromSyncPayload(payload))

    @Test
    fun pantryItemRoundTrips() {
        val dto = PantryItemDto(
            id = "p1",
            userId = "u1",
            ingredientId = "i1",
            quantity = "200g",
            expiryDate = "2026-07-01",
            addedDate = "2026-06-25T10:15:30Z",
            consumed = true,
            consumedDate = "2026-06-26T08:00:00Z",
        )
        assertEquals(dto, roundTrip(dto))
    }

    @Test
    fun pantryItemWithDefaultsRoundTrips() {
        // The converter does not write default values, so this checks they come back as defaults
        // (quantity/expiry null, consumed false) and not as something else.
        val dto = PantryItemDto(id = "p2", userId = "u1", ingredientId = "i1", addedDate = "2026-06-25T10:15:30Z")
        assertEquals(dto, roundTrip(dto))
    }

    @Test
    fun favoriteRoundTrips() {
        val dto = FavoriteDto(userId = "u1", recipeId = "r1", savedDate = "2026-06-25T10:15:30Z")
        assertEquals(dto, roundTrip(dto))
    }

    @Test
    fun cookedRecipeRoundTrips() {
        val dto = CookedRecipeDto(id = "c1", userId = "u1", recipeId = "r1", cookedDate = "2026-06-25T10:15:30Z")
        assertEquals(dto, roundTrip(dto))
    }

    @Test
    fun ingredientRoundTrips() {
        val dto = IngredientDto(id = "i9", name = "pomodoro", createdByUser = true, userId = "u1")
        assertEquals(dto, roundTrip(dto))
    }

    @Test
    fun syncActionRoundTrips() {
        for (action in SyncAction.entries) {
            assertEquals(action, Converters.toSyncAction(Converters.fromSyncAction(action)))
        }
    }
}
