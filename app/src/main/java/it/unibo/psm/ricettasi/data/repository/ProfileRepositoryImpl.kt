package it.unibo.psm.ricettasi.data.repository

import it.unibo.psm.ricettasi.data.local.dao.BadgeDao
import it.unibo.psm.ricettasi.data.local.dao.CookedRecipeDao
import it.unibo.psm.ricettasi.data.local.dao.FavoriteDao
import it.unibo.psm.ricettasi.data.local.dao.PantryDao
import it.unibo.psm.ricettasi.data.local.dao.ProfileDao
import it.unibo.psm.ricettasi.data.mapper.toDomain
import it.unibo.psm.ricettasi.domain.model.Badge
import it.unibo.psm.ricettasi.domain.model.UserBadge
import it.unibo.psm.ricettasi.domain.model.UserProfile
import it.unibo.psm.ricettasi.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
    private val badgeDao: BadgeDao,
    private val pantryDao: PantryDao,
    private val favoriteDao: FavoriteDao,
    private val cookedRecipeDao: CookedRecipeDao,
) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfile?> =
        profileDao.observeProfile().map { it?.toDomain() }

    override fun observeBadges(): Flow<List<Badge>> =
        badgeDao.observeBadges().map { list -> list.map { it.toDomain() } }

    override fun observeUnlockedBadges(): Flow<List<UserBadge>> =
        badgeDao.observeUserBadges().map { list -> list.map { it.toDomain() } }

    override fun observePantryCount(): Flow<Int> = pantryDao.observeActiveCount()

    override fun observeFavoriteCount(): Flow<Int> = favoriteDao.observeCount()

    override fun observeCookedUniqueCount(): Flow<Int> = cookedRecipeDao.observeUniqueCount()
}
