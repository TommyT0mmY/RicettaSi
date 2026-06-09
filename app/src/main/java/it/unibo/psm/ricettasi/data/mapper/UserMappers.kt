package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.entity.BadgeEntity
import it.unibo.psm.ricettasi.data.local.entity.UserBadgeEntity
import it.unibo.psm.ricettasi.data.local.entity.UserProfileEntity
import it.unibo.psm.ricettasi.data.remote.dto.BadgeDto
import it.unibo.psm.ricettasi.data.remote.dto.UserBadgeDto
import it.unibo.psm.ricettasi.data.remote.dto.UserProfileDto
import it.unibo.psm.ricettasi.domain.model.Badge
import it.unibo.psm.ricettasi.domain.model.UserBadge
import it.unibo.psm.ricettasi.domain.model.UserProfile
import java.time.Instant

// ---------- UserProfile ----------
fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    userId = userId,
    displayName = displayName,
    avatarEmoji = avatarEmoji,
    xp = xp,
    level = level,
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    userId = userId,
    displayName = displayName,
    avatarEmoji = avatarEmoji,
    xp = xp,
    level = level,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    userId = userId,
    displayName = displayName,
    avatarEmoji = avatarEmoji,
    xp = xp,
    level = level,
)

// ---------- Badge ----------
fun BadgeDto.toDomain(): Badge = Badge(id = id, code = code, name = name, description = description)

fun BadgeEntity.toDomain(): Badge = Badge(id = id, code = code, name = name, description = description)

fun Badge.toEntity(): BadgeEntity = BadgeEntity(id = id, code = code, name = name, description = description)

// ---------- UserBadge ----------
fun UserBadgeDto.toDomain(): UserBadge = UserBadge(badgeId = badgeId, unlockedDate = unlockedDate.toInstant())
fun UserBadge.toDto(userId: String): UserBadgeDto =
    UserBadgeDto(userId = userId, badgeId = badgeId, unlockedDate = unlockedDate.toString())
fun UserBadgeEntity.toDomain(): UserBadge = UserBadge(badgeId = badgeId, unlockedDate = Instant.ofEpochMilli(unlockedDate))
fun UserBadge.toEntity(): UserBadgeEntity = UserBadgeEntity(badgeId = badgeId, unlockedDate = unlockedDate.toEpochMilli())
