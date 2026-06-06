package it.unibo.psm.ricettasi.domain.model

/**
 * A single ingredient, either from the curated list or added by a user who typed a name
 * we didn't already have ([createdByUser] tells which case).
 *
 * Synonyms (e.g. "passata" and "passata di pomodoro") count as the same ingredient in the
 * pantry. Instead of merging them, they're linked through [parentIngredientId]: the one
 * with no parent is the real one, the rest just point to it, only one level deep.
 */
data class Ingredient(
    val id: String,
    val name: String,
    val parentIngredientId: String? = null,
    // true only for ingredients added manually by a user, false for the curated list
    val createdByUser: Boolean = false,
)
