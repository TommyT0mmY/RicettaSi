package it.unibo.psm.ricettasi.data.repository

import it.unibo.psm.ricettasi.data.local.entity.IngredientEntity

/**
 * Resolves an ingredient to the representative (root) of its synonym group by following
 * `parentIngredientId` until a row with a null parent is reached. [byId] is the indexed ingredient map.
 * Cheap: the map is in memory and the chain is at most 1 level deep, but a `visited` set guards
 * against accidental cycles just in case.
 */
internal fun resolveRoot(ingredientId: String, byId: Map<String, IngredientEntity>): String {
    var current = ingredientId
    val visited = HashSet<String>()
    while (visited.add(current)) {
        val parent = byId[current]?.parentIngredientId ?: return current
        current = parent
    }
    return current // defensive guard against cycles (should never occur)
}
