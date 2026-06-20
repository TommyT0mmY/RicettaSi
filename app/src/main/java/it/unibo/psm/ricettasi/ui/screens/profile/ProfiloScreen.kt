package it.unibo.psm.ricettasi.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.R
import it.unibo.psm.ricettasi.domain.model.xpForLevel
import it.unibo.psm.ricettasi.ui.components.BadgeIcon
import it.unibo.psm.ricettasi.ui.theme.CardElevation
import it.unibo.psm.ricettasi.ui.theme.FrauncesFamily
import it.unibo.psm.ricettasi.ui.theme.ManropeFamily
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import it.unibo.psm.ricettasi.ui.theme.SpaceXs
import org.koin.androidx.compose.koinViewModel

// -- Brand accent colours (used across the profile screen) --

private val AccentColor = Color(0xFFE65F2B)
private val AccentDarkVariant = Color(0xFFFF8A58)

// -- Route --

/** Connects [ProfiloViewModel] to the UI and provides navigation callbacks. */
@Composable
fun ProfiloRoute(
    onNavigateToSettings: () -> Unit,
    viewModel: ProfiloViewModel = koinViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val badgeDisplays by viewModel.badgeDisplays.collectAsStateWithLifecycle()
    val pantryCount by viewModel.pantryCount.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()
    val cookedUniqueCount by viewModel.cookedUniqueCount.collectAsStateWithLifecycle()

    ProfiloScreen(
        profile = profile,
        badgeDisplays = badgeDisplays,
        pantryCount = pantryCount,
        favoriteCount = favoriteCount,
        cookedUniqueCount = cookedUniqueCount,
        onNavigateToSettings = onNavigateToSettings,
    )
}

// -- Main screen --

@Composable
private fun ProfiloScreen(
    profile: it.unibo.psm.ricettasi.domain.model.UserProfile?,
    badgeDisplays: List<BadgeDisplay>,
    pantryCount: Int,
    favoriteCount: Int,
    cookedUniqueCount: Int,
    onNavigateToSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        item {
            ProfileHeader(onSettingsClick = onNavigateToSettings)
        }

        // Hero card
        item {
            if (profile != null) {
                HeroCard(profile = profile)
            }
        }

        // Stats row
        item {
            StatsRow(
                cookedCount = cookedUniqueCount,
                favoriteCount = favoriteCount,
                pantryCount = pantryCount,
            )
        }

        // Trophies section
        item {
            TrophiesSection(badgeDisplays = badgeDisplays)
        }
    }
}

// -- Header --

@Composable
private fun ProfileHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SpaceXl, end = SpaceXl, top = SpaceXl, bottom = SpaceMd),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Profilo",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Impostazioni",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onSettingsClick),
        )
    }
}

// -- Hero card --

@Composable
private fun HeroCard(profile: it.unibo.psm.ricettasi.domain.model.UserProfile) {
    val currentLevel = profile.level
    val currentXp = profile.xp
    val currentLevelXp = xpForLevel(currentLevel)
    val nextLevelXp = xpForLevel(currentLevel + 1)
    val progress = ((currentXp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp).toFloat())
        .coerceIn(0f, 1f)
    val initial = profile.displayName.firstOrNull()?.uppercase() ?: "?"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl, vertical = SpaceMd),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            // Top row: avatar, text block, trophy icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AccentColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.width(SpaceLg))

                // Name and level
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVELLO $currentLevel",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = profile.displayName,
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Trophy icon (right)
                Icon(
                    painter = painterResource(id = R.drawable.ic_trophy),
                    contentDescription = "Trofei",
                    tint = AccentDarkVariant,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(Space2xl))

            // XP bar
            XpBar(
                currentXp = currentXp,
                currentLevelXp = currentLevelXp,
                nextLevelXp = nextLevelXp,
                progress = progress,
            )
        }
    }
}

// -- XP bar --

@Composable
private fun XpBar(
    currentXp: Int,
    currentLevelXp: Int,
    nextLevelXp: Int,
    progress: Float,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // XP labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$currentXp XP",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$nextLevelXp XP",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(SpaceXs))

        // Track and fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(RoundedLg))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(RoundedLg))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AccentColor, AccentDarkVariant),
                        ),
                    ),
            )
        }
    }
}

// -- Stats row --

@Composable
private fun StatsRow(
    cookedCount: Int,
    favoriteCount: Int,
    pantryCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl, vertical = SpaceMd),
        horizontalArrangement = Arrangement.spacedBy(SpaceLg),
    ) {
        StatCard(
            iconRes = R.drawable.ic_chef_hat,
            count = cookedCount,
            label = "CUCINATE",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            iconRes = R.drawable.ic_flame_heart,
            count = favoriteCount,
            label = "PREFERITI",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            iconRes = R.drawable.ic_fridge,
            count = pantryCount,
            label = "DISPENSA",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    iconRes: Int,
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpaceXl, horizontal = SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = AccentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(SpaceMd))

            Text(
                text = count.toString(),
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// -- Trophies section --

@Composable
private fun TrophiesSection(badgeDisplays: List<BadgeDisplay>) {
    val unlockedCount = badgeDisplays.count { it.unlocked }
    val totalCount = badgeDisplays.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl, vertical = SpaceMd),
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_medal),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(SpaceMd))
                Text(
                    text = "Trofei",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "$unlockedCount/$totalCount",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(SpaceLg))

        if (badgeDisplays.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space2xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Inizia a cucinare",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(SpaceXs))
                Text(
                    text = "Completa le sfide e cucina nuove ricette per sbloccare i trofei.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            // Trophy grid: 2 columns, natural heights
            val rows = badgeDisplays.chunked(2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpaceLg),
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpaceLg),
                    ) {
                        rowItems.forEach { display ->
                            Box(modifier = Modifier.weight(1f)) {
                                TrophyCard(display = display)
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single trophy card in the 2-column grid.
 *
 * Unlocked trophies show full-colour artwork with a subtle accent border;
 * locked trophies are rendered desaturated on a plain white background.
 */
@Composable
private fun TrophyCard(display: BadgeDisplay) {
    val borderColor = if (display.unlocked) {
        AccentColor.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (display.unlocked) {
                    Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(RoundedLg),
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BadgeIcon(
                badge = display.badge,
                unlocked = display.unlocked,
                size = 56.dp,
            )
            Spacer(Modifier.height(SpaceXs))

            Text(
                text = display.badge.name,
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                color = if (display.unlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                textAlign = TextAlign.Center,
            )

            Text(
                text = display.badge.description ?: "",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                maxLines = 2,
                color = if (display.unlocked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
                },
                textAlign = TextAlign.Center,
            )
        }
    }
}
