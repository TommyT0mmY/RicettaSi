package it.unibo.psm.ricettasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.ExpiryStatus
import it.unibo.psm.ricettasi.domain.model.RecipeSummary
import it.unibo.psm.ricettasi.domain.model.TimeWindow
import it.unibo.psm.ricettasi.ui.components.RecipeImage
import it.unibo.psm.ricettasi.ui.components.RecipeListCard
import it.unibo.psm.ricettasi.ui.components.displayLabel
import it.unibo.psm.ricettasi.ui.screens.esplora.EsploraPreset
import it.unibo.psm.ricettasi.ui.screens.pantry.PantryItemDisplay
import it.unibo.psm.ricettasi.ui.theme.CardElevation
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.RoundedMd
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceSm
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import it.unibo.psm.ricettasi.ui.theme.customColors
import org.koin.androidx.compose.koinViewModel

private val TimeSlot.sectionIcon: ImageVector
    get() = when (this) {
        TimeSlot.MATTINA -> Icons.Outlined.WbSunny
        TimeSlot.PRANZO -> Icons.Outlined.Restaurant
        TimeSlot.POMERIGGIO -> Icons.Outlined.LocalCafe
        TimeSlot.CENA -> Icons.Outlined.Restaurant
        TimeSlot.NOTTE -> Icons.Outlined.NightsStay
    }

@Composable
fun HomeRoute(
    onNavigateToRecipe: (String) -> Unit = {},
    onNavigateToSvuotaIlFrigo: () -> Unit = {},
    onNavigateToPantry: () -> Unit = {},
    onNavigateToEsplora: (EsploraPreset) -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRecipeClick = onNavigateToRecipe,
        onSvuotaIlFrigoClick = onNavigateToSvuotaIlFrigo,
        onToggleFavorite = viewModel::toggleFavorite,
        onSeeAllExpiring = onNavigateToPantry,
        onNavigateToEsplora = onNavigateToEsplora,
        onNavigateToFavorites = onNavigateToFavorites,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRecipeClick: (String) -> Unit,
    onSvuotaIlFrigoClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSeeAllExpiring: () -> Unit,
    onNavigateToEsplora: (EsploraPreset) -> Unit,
    onNavigateToFavorites: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            HeroSection(
                timeSlot = state.timeSlot,
                modifier = Modifier
                    .padding(horizontal = SpaceXl)
                    .padding(top = Space2xl, bottom = Space2xl),
            )
        }

        if (state.expiringItems.isNotEmpty()) {
            item {
                SvuotaIlFrigoCard(
                    onClick = onSvuotaIlFrigoClick,
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(28.dp)) }
            item {
                SectionHeader(
                    title = "In scadenza",
                    icon = Icons.Outlined.Eco,
                    onSeeAll = onSeeAllExpiring,
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(SpaceLg)) }
            item {
                ExpiringItemsRow(
                    items = state.expiringItems,
                    modifier = Modifier.padding(start = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(28.dp)) }
        }

        if (state.suggestions.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Dalla tua dispensa",
                    icon = Icons.Outlined.Stars,
                    onSeeAll = { onNavigateToEsplora(EsploraPreset()) },
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(SpaceLg)) }
            items(state.suggestions.take(5), key = { "suggestion_${it.recipe.id}" }) { item ->
                RecipeListCard(
                    item = item,
                    isFavorite = item.recipe.id in state.favoriteIds,
                    onToggleFavorite = onToggleFavorite,
                    onClick = { onRecipeClick(item.recipe.id) },
                    modifier = Modifier
                        .padding(horizontal = SpaceXl)
                        .padding(bottom = SpaceLg),
                )
            }
            item { Spacer(Modifier.height(SpaceXl)) }
        }

        if (state.favorites.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Preferite",
                    icon = Icons.Outlined.FavoriteBorder,
                    onSeeAll = onNavigateToFavorites,
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(SpaceLg)) }
            item {
                RecipeScrollRow(
                    recipes = state.favorites,
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.padding(start = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(28.dp)) }
        }

        if (state.timeBasedRecipes.isNotEmpty()) {
            item {
                SectionHeader(
                    title = state.timeSlot.sectionTitle,
                    icon = state.timeSlot.sectionIcon,
                    onSeeAll = { onNavigateToEsplora(EsploraPreset(mealType = state.timeSlot.mealType)) },
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(SpaceLg)) }
            items(state.timeBasedRecipes.take(4), key = { "timeslot_${it.recipe.id}" }) { item ->
                RecipeListCard(
                    item = item,
                    isFavorite = item.recipe.id in state.favoriteIds,
                    onToggleFavorite = onToggleFavorite,
                    onClick = { onRecipeClick(item.recipe.id) },
                    modifier = Modifier
                        .padding(horizontal = SpaceXl)
                        .padding(bottom = SpaceLg),
                )
            }
            item { Spacer(Modifier.height(SpaceXl)) }
        }

        if (state.quickRecipes.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Pronte in 15 minuti",
                    icon = Icons.Outlined.Timer,
                    onSeeAll = { onNavigateToEsplora(EsploraPreset(timeWindow = TimeWindow.QUICK)) },
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(SpaceLg)) }
            item {
                RecipeScrollRow(
                    recipes = state.quickRecipes.take(6).map { it.recipe },
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.padding(start = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(28.dp)) }
        }

        if (state.challengeRecipes.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Mettiti alla prova",
                    icon = Icons.Outlined.EmojiEvents,
                    onSeeAll = { onNavigateToEsplora(EsploraPreset(difficulties = setOf(Difficulty.MEDIO, Difficulty.DIFFICILE))) },
                    modifier = Modifier.padding(horizontal = SpaceXl),
                )
            }
            item { Spacer(Modifier.height(SpaceLg)) }
            items(state.challengeRecipes.take(4), key = { "challenge_${it.recipe.id}" }) { item ->
                RecipeListCard(
                    item = item,
                    isFavorite = item.recipe.id in state.favoriteIds,
                    onToggleFavorite = onToggleFavorite,
                    onClick = { onRecipeClick(item.recipe.id) },
                    modifier = Modifier
                        .padding(horizontal = SpaceXl)
                        .padding(bottom = SpaceLg),
                )
            }
        }
    }
}

@Composable
private fun HeroSection(timeSlot: TimeSlot, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = timeSlot.heroLine1,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = timeSlot.heroLine2,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SvuotaIlFrigoCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Svuota il frigo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(SpaceSm))
                Text(
                    text = "Ricette con quello che sta per scadere",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            Spacer(Modifier.width(SpaceXl))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(SpaceMd))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "Vedi tutto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ExpiringItemsRow(items: List<PantryItemDisplay>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpaceMd),
        contentPadding = PaddingValues(end = SpaceXl),
    ) {
        items(items, key = { it.pantryItem.id }) { item ->
            ExpiringItemPill(item = item)
        }
    }
}

@Composable
private fun ExpiringItemPill(item: PantryItemDisplay) {
    val expiryLabel = when (item.daysUntilExpiry) {
        null -> ""
        0L -> "Oggi"
        1L -> "Domani"
        else -> "${item.daysUntilExpiry} giorni"
    }
    val statusColor = when (item.expiryStatus) {
        ExpiryStatus.CRITICAL -> MaterialTheme.customColors.statusCritical
        ExpiryStatus.WARNING -> MaterialTheme.customColors.statusWarning
        ExpiryStatus.SECONDARY -> MaterialTheme.customColors.statusWarning
        else -> MaterialTheme.customColors.statusOk
    }

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(RoundedMd))
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = SpaceLg),
    ) {
        Text(
            text = item.ingredientName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = expiryLabel,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                maxLines = 1,
            )
            if (item.pantryItem.quantity != null) {
                Text(
                    text = item.pantryItem.quantity!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun RecipeScrollRow(
    recipes: List<RecipeSummary>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpaceLg),
        contentPadding = PaddingValues(end = SpaceXl),
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeScrollCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
        }
    }
}

@Composable
private fun RecipeScrollCard(recipe: RecipeSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        onClick = onClick,
    ) {
        Column {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(SpaceSm))
                val meta = buildList {
                    if (recipe.preparationTime != null) add("${recipe.preparationTime} min")
                    add(recipe.difficulty.displayLabel)
                }.joinToString(" · ")
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

