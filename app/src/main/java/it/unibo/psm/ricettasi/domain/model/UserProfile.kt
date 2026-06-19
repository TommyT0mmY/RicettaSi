package it.unibo.psm.ricettasi.domain.model

import kotlin.math.floor
import kotlin.math.sqrt

// xp needed to reach level L is XP_LEVEL_SCALE * (L-1)^2
private const val XP_LEVEL_SCALE = 100

/** Total xp required to have reached [level] (level 1 starts at 0 xp). */
fun xpForLevel(level: Int): Int = XP_LEVEL_SCALE * (level - 1) * (level - 1)

/** The level a given total xp amount falls into. Inverse of [xpForLevel]. */
fun levelForXp(xp: Int): Int = floor(sqrt(xp / XP_LEVEL_SCALE.toDouble())).toInt() + 1

/** How much xp cooking a recipe is worth, based on how hard it is. */
fun xpRewardFor(difficulty: Difficulty): Int = when (difficulty) {
    Difficulty.FACILE -> 10
    Difficulty.MEDIO -> 20
    Difficulty.DIFFICILE -> 30
}

/**
 * The logged-in user's profile, gamification stats included. Badges themselves are tracked
 * separately, this only holds xp and level.
 */
data class UserProfile(
    val userId: String,
    val displayName: String,
    val xp: Int = 0,
    val level: Int = levelForXp(xp),
)
