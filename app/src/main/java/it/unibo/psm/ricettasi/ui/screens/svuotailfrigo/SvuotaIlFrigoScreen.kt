package it.unibo.psm.ricettasi.ui.screens.svuotailfrigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.ui.components.RecipeListCard
import it.unibo.psm.ricettasi.ui.theme.IconBtn
import it.unibo.psm.ricettasi.ui.theme.RoundedFull
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import org.koin.androidx.compose.koinViewModel

// -- Route --

@Composable
fun SvuotaIlFrigoRoute(
    onBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    viewModel: SvuotaIlFrigoViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SvuotaIlFrigoScreen(
        state = state,
        onBack = onBack,
        onRecipeClick = onNavigateToRecipe,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

// -- Screen --

@Composable
private fun SvuotaIlFrigoScreen(
    state: SvuotaIlFrigoUiState,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = Space2xl),
    ) {
        item {
            PageHeader(onBack = onBack)
        }

        item {
            ExpiringInfoBox(
                names = state.expiringNames,
                modifier = Modifier
                    .padding(horizontal = SpaceXl)
                    .padding(top = SpaceXl),
            )
        }

        if (state.recipes.isNotEmpty()) {
            item {
                Text(
                    text = recipeCountLabel(state.recipes.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = SpaceXl)
                        .padding(top = Space2xl, bottom = SpaceLg),
                )
            }
            items(state.recipes, key = { it.recipe.id }) { item ->
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
        } else if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Space2xl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            item {
                Text(
                    text = "Nessuna ricetta trovata. Prova ad aggiungere altri ingredienti alla dispensa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = SpaceXl)
                        .padding(top = Space2xl),
                )
            }
        }
    }
}

/** "1 RICETTA" / "N RICETTE", the overline label above the results. */
private fun recipeCountLabel(count: Int): String =
    if (count == 1) "1 RICETTA" else "$count RICETTE"

// -- Page header (title + subtitle + back on the right) --

@Composable
private fun PageHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl)
            .padding(top = Space2xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Svuota il frigo",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(SpaceMd))
            Text(
                text = "Ricette con ciò che scade presto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(IconBtn),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// -- Info box with the expiring ingredient chips --

@Composable
private fun ExpiringInfoBox(names: List<String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RoundedLg))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(SpaceXl),
    ) {
        Text(
            text = "Ingredienti in scadenza",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(SpaceLg))
        if (names.isEmpty()) {
            Text(
                text = "Niente in scadenza, ottimo lavoro!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpaceMd),
                verticalArrangement = Arrangement.spacedBy(SpaceMd),
            ) {
                names.forEach { name ->
                    ExpiringChip(name = name)
                }
            }
        }
    }
}

@Composable
private fun ExpiringChip(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(RoundedFull))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(RoundedFull),
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
