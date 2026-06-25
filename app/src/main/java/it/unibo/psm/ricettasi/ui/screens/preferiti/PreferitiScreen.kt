package it.unibo.psm.ricettasi.ui.screens.preferiti

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import it.unibo.psm.ricettasi.domain.model.toSummary
import it.unibo.psm.ricettasi.ui.components.RecipeListCard
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceSm
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import org.koin.androidx.compose.koinViewModel

@Composable
fun PreferitiRoute(
    onNavigateToRecipe: (String) -> Unit = {},
) {
    val viewModel: PreferitiViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PreferitiScreen(
        state = state,
        onRecipeClick = onNavigateToRecipe,
        onRemoveFavorite = viewModel::removeFavorite,
    )
}

@Composable
private fun PreferitiScreen(
    state: PreferitiUiState,
    onRecipeClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = SpaceXl, end = SpaceXl, top = Space2xl, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(SpaceXl),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = SpaceMd)) {
                Text(
                    text = "Preferiti",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(SpaceSm))
                Text(
                    text = if (state.recipes.isEmpty()) "Nessuna ricetta salvata"
                    else if (state.recipes.size == 1) "1 ricetta salvata"
                    else "${state.recipes.size} ricette salvate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.recipes.isEmpty()) {
            item {
                EmptyFavorites()
            }
        } else {
            items(state.recipes, key = { "fav_${it.id}" }) { recipe ->
                val item = RecipeWithAvailability(
                    recipe = recipe.toSummary(),
                    availableCount = 0,
                    requiredCount = 0,
                )
                RecipeListCard(
                    item = item,
                    isFavorite = true,
                    onToggleFavorite = { onRemoveFavorite(recipe.id) },
                    onClick = { onRecipeClick(recipe.id) },
                )
            }
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(SpaceXl))
        Text(
            text = "Ancora nessun preferito",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(SpaceMd))
        Text(
            text = "Salva le ricette che ti piacciono e le ritroverai qui.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
