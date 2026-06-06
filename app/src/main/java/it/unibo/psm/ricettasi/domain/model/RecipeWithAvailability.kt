package it.unibo.psm.ricettasi.domain.model

/**
 * Wraps a [RecipeSummary] with how many of its ingredients are available and how many of
 * those are expiring soon.
 */
data class RecipeWithAvailability(
    val recipe: RecipeSummary,
    val availableCount: Int,
    val requiredCount: Int,
    // separate from matchPercent because "Svuota il frigo" ranks by expiringMatchCount,
    // while "Per te" ranks by overall match
    val expiringMatchCount: Int = 0,
) {
    /** Match percentage shown on the recipe card. */
    val matchPercent: Int
        get() = if (requiredCount == 0) 0
        else (availableCount * 100) / requiredCount
}

/**
 * Preparation time buckets for the search filters. [code] is the value sent to the
 * backend, [label] is what's shown on the filter chip.
 */
enum class TimeWindow(val code: String, val label: String) {
    QUICK("quick",  "≤ 15 min"),
    MEDIUM("medium", "15-30 min"),
    LONG("long",   "> 30 min"),
}

/**
 * Active search filters. [mealType] picks at most one value. [difficulties] and
 * [timeWindows] match on OR (any selected value is enough), [categories] and
 * [ingredientIds] match on AND (the recipe must satisfy all selected values).
 */
data class RecipeFilters(
    val mealType: MealType? = null,
    val difficulties: Set<Difficulty> = emptySet(),
    val timeWindows: Set<TimeWindow> = emptySet(),
    val categories: Set<String> = emptySet(),
    val ingredientIds: Set<String> = emptySet(),
) {
    val isEmpty: Boolean
        get() = mealType == null && difficulties.isEmpty() && timeWindows.isEmpty() &&
            categories.isEmpty() && ingredientIds.isEmpty()
}
