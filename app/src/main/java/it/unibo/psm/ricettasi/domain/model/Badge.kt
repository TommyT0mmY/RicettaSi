package it.unibo.psm.ricettasi.domain.model

import java.time.Instant

/**
 * One of the trophies in the profile's badge grid. The unlock condition (cook N recipes,
 * fill the pantry, etc.) is decided and checked entirely on the backend, the client only
 * gets told whether it's unlocked or not. So this class carries no logic, just what to display.
 *
 * Icons are bundled SVGs, each badge has its own icon named [code].
 * There is a fallback generic icon if a new badge ships server side before its drawable is obtained
 * through an update.
 */
data class Badge(
    val id: String,
    val code: String, // matches a bundled drawable name, this is how the UI picks which icon to show
    val name: String,
    val description: String? = null,
)

/** Records that the current user unlocked a given badge, and when it happened. */
data class UserBadge(
    val badgeId: String,
    val unlockedDate: Instant,
)
