package it.unibo.psm.ricettasi.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.Badge
import it.unibo.psm.ricettasi.domain.model.UserBadge
import it.unibo.psm.ricettasi.domain.model.UserProfile
import it.unibo.psm.ricettasi.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * A badge paired with its unlock status for display in the trophies grid.
 */
data class BadgeDisplay(
    val badge: Badge,
    val unlocked: Boolean,
)

/**
 * ViewModel for the Profilo screen.
 *
 * Observes the profile repository reactively so gamification stats and badges
 * stay up to date across sync rounds.
 */
class ProfiloViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _badgeDisplays = MutableStateFlow<List<BadgeDisplay>>(emptyList())
    val badgeDisplays: StateFlow<List<BadgeDisplay>> = _badgeDisplays.asStateFlow()

    private val _pantryCount = MutableStateFlow(0)
    val pantryCount: StateFlow<Int> = _pantryCount.asStateFlow()

    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()

    private val _cookedUniqueCount = MutableStateFlow(0)
    val cookedUniqueCount: StateFlow<Int> = _cookedUniqueCount.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().collect { _profile.value = it }
        }
        viewModelScope.launch {
            combine(
                profileRepository.observeBadges(),
                profileRepository.observeUnlockedBadges(),
            ) { badges, unlocked ->
                val unlockedIds = unlocked.map { it.badgeId }.toSet()
                badges.map { badge ->
                    BadgeDisplay(badge = badge, unlocked = badge.id in unlockedIds)
                }
            }.collect { _badgeDisplays.value = it }
        }
        viewModelScope.launch {
            profileRepository.observePantryCount().collect { _pantryCount.value = it }
        }
        viewModelScope.launch {
            profileRepository.observeFavoriteCount().collect { _favoriteCount.value = it }
        }
        viewModelScope.launch {
            profileRepository.observeCookedUniqueCount().collect { _cookedUniqueCount.value = it }
        }
    }
}
