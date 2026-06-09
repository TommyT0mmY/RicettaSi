package it.unibo.psm.ricettasi.domain.repository

import it.unibo.psm.ricettasi.domain.model.Badge
import it.unibo.psm.ricettasi.domain.model.UserBadge
import it.unibo.psm.ricettasi.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** Current user's profile, gamification stats and badges, all kept up to date. */
interface ProfileRepository {

    /** Reactive: sync pull updates XP/level while Profilo screen is open. */
    fun observeProfile(): Flow<UserProfile?>

    /** Reactive: sync pull updates badge list while Profilo screen is open. */
    fun observeBadges(): Flow<List<Badge>>

    /** Reactive: sync pull updates unlocked badges while Profilo screen is open. */
    fun observeUnlockedBadges(): Flow<List<UserBadge>>

    /** Reactive: count of active pantry items for the Profile stats row. */
    fun observePantryCount(): Flow<Int>

    /** Reactive: count of favourite recipes for the Profile stats row. */
    fun observeFavoriteCount(): Flow<Int>

    /** Reactive: count of unique cooked recipes for the Profile stats row. */
    fun observeCookedUniqueCount(): Flow<Int>
}
