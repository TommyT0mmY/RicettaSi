package it.unibo.psm.ricettasi.domain.model

/**
 * Stored in Italian since that's also what gets shown on screen.
 * [fromValue] falls back to [FACILE] instead of returning null, so a typo on the data side
 * doesn't break the whole recipe card.
 */
enum class Difficulty(val value: String) {
    FACILE("facile"),
    MEDIO("medio"),
    DIFFICILE("difficile");

    companion object {
        fun fromValue(value: String): Difficulty =
            entries.firstOrNull { it.value == value } ?: FACILE
    }
}

/**
 * Same deal as [Difficulty], value is the Italian label. Unlike difficulty though, [fromValue]
 * returns null here instead of defaulting to something, because not having a meal type is a
 * perfectly normal state for a recipe (e.g. a generic snack), so there's no "default" that
 * would make sense.
 */
enum class MealType(val value: String) {
    COLAZIONE("colazione"),
    PRANZO("pranzo"),
    CENA("cena"),
    BRUNCH("brunch"),
    MERENDA("merenda"),
    APERITIVO("aperitivo");

    companion object {
        fun fromValue(value: String): MealType? =
            entries.firstOrNull { it.value == value }
    }
}

/**
 * One ingredient line as written by the recipe's author. Two recipes that both call for tomato
 * point at the same [ingredientId], even if one wants "2 pomodori interi" and the other "passata
 * di pomodoro".
 *
 * [quantity] is only ever shown to the user as-is ("200g", "q.b.", "2 cucchiai") and the app
 * never parses or sums it. Whether a recipe counts as available only checks if the ingredient
 * itself is in the pantry, the amount doesn't matter.
 */
data class RecipeIngredient(
    val ingredientId: String,
    val quantity: String? = null,
    /** Resolved at display time from the API embed or local ingredient table. */
    val name: String? = null,
)

/**
 * A full recipe, already assembled with its steps and ingredients.
 *
 * [mealTypes] and [categories] are many-to-many relationships flattened into plain lists by the
 * time the data reaches this model.
 */
data class Recipe(
    val id: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    // in minutes, shown on the card as e.g. "30 min"
    val preparationTime: Int? = null,
    val difficulty: Difficulty = Difficulty.FACILE,
    // shown numbered on screen using the list position, no need to carry a step number around
    val steps: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val mealTypes: List<MealType> = emptyList(),
    val categories: List<String> = emptyList(),
    // null for the recipes we ship with the app, set to the author's user id for user-submitted ones
    val createdByUserId: String? = null,
    // free-text yield: "4 persone", "20-25 biscotti", "8 fette", "1 barattolo", ...
    val servings: String? = null,
)

/**
 * Just what a recipe card on a list screen shows, no steps and no ingredient lines.
 * Search results and suggestion lists return this lighter shape instead of a full [Recipe],
 * since they never need the detailed content.
 */
data class RecipeSummary(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val preparationTime: Int? = null,
    val difficulty: Difficulty = Difficulty.FACILE,
    val mealTypes: List<MealType> = emptyList(),
    val categories: List<String> = emptyList(),
)

/** For screens (preferiti, dettaglio) that hold a full [Recipe] but only need to show a card. */
fun Recipe.toSummary(): RecipeSummary = RecipeSummary(
    id = id,
    title = title,
    imageUrl = imageUrl,
    preparationTime = preparationTime,
    difficulty = difficulty,
    mealTypes = mealTypes,
    categories = categories,
)
