package it.unibo.psm.ricettasi.ui.screens.esplora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.Ingredient
import it.unibo.psm.ricettasi.domain.model.MealType
import it.unibo.psm.ricettasi.domain.model.TimeWindow
import it.unibo.psm.ricettasi.ui.components.RecipeListCard
import it.unibo.psm.ricettasi.ui.components.displayLabel
import it.unibo.psm.ricettasi.ui.theme.FrauncesFamily
import it.unibo.psm.ricettasi.ui.theme.RoundedFull
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import org.koin.androidx.compose.koinViewModel

private val MealType.displayLabel: String
    get() = value.replaceFirstChar { it.uppercase() }

@Composable
fun EsploraRoute(onNavigateToRecipe: (String) -> Unit = {}) {
    val viewModel: EsploraViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // System back moves up one page in the flow instead of leaving the tab.
    BackHandler(enabled = state.mode != EsploraMode.MAIN) {
        when (state.mode) {
            EsploraMode.FILTERS -> viewModel.applyAndShowResults()
            EsploraMode.INGREDIENT_SEARCH, EsploraMode.CATEGORY_SEARCH -> viewModel.backToFilters()
            EsploraMode.MAIN -> Unit
        }
    }

    when (state.mode) {
        EsploraMode.MAIN -> MainContent(
            state = state,
            onOpenSearch = viewModel::openSearch,
            onOpenFilters = viewModel::openFilters,
            onClearQuery = { viewModel.onQueryChanged("") },
            onRecipeClick = onNavigateToRecipe,
            onToggleFavorite = viewModel::toggleFavorite,
        )
        EsploraMode.FILTERS -> FiltersContent(
            state = state,
            onBack = viewModel::applyAndShowResults,
            onQueryChanged = viewModel::onQueryChanged,
            onConsumeSearchFocus = viewModel::consumeSearchFocus,
            onOpenIngredientSearch = viewModel::openIngredientSearch,
            onRemoveIngredient = viewModel::removeIngredient,
            onOpenCategorySearch = viewModel::openCategorySearch,
            onRemoveCategory = viewModel::removeCategory,
            onToggleMealType = viewModel::toggleMealType,
            onToggleTimeWindow = viewModel::toggleTimeWindow,
            onToggleDifficulty = viewModel::toggleDifficulty,
            onReset = viewModel::reset,
            onShowResults = viewModel::applyAndShowResults,
        )
        EsploraMode.INGREDIENT_SEARCH -> IngredientSearchContent(
            state = state,
            onBack = viewModel::backToFilters,
            onQueryChanged = viewModel::onIngredientQueryChanged,
            onAddSuggestion = viewModel::addIngredient,
            onTogglePantry = viewModel::toggleIngredient,
        )
        EsploraMode.CATEGORY_SEARCH -> CategorySearchContent(
            state = state,
            onBack = viewModel::backToFilters,
            onQueryChanged = viewModel::onCategoryQueryChanged,
            onToggle = viewModel::toggleCategory,
        )
    }
}

// -- 1. Main page: search bar + recipe list --

@Composable
private fun MainContent(
    state: EsploraUiState,
    onOpenSearch: () -> Unit,
    onOpenFilters: () -> Unit,
    onClearQuery: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = "Esplora",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = SpaceXl, end = SpaceXl, top = Space2xl, bottom = SpaceXl),
        )
        FakeSearchBar(
            query = state.query,
            placeholder = "Cerca ricette, ingredienti...",
            onClick = onOpenSearch,
            onClear = onClearQuery,
            modifier = Modifier.padding(horizontal = SpaceXl),
        )
        Spacer(Modifier.height(SpaceLg))
        val shortcutChips = listOf(
            "Ingredienti" to state.selectedIngredients.size,
            "Tipo di pasto" to (if (state.mealType != null) 1 else 0),
            "Categorie" to state.selectedCategories.size,
            "Tempo" to state.timeWindows.size,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = SpaceXl),
            horizontalArrangement = Arrangement.spacedBy(SpaceMd),
        ) {
            items(shortcutChips, key = { it.first }) { (label, count) ->
                val display = if (count > 0) "$label ($count)" else label
                FilterChip(label = display, selected = count > 0, onClick = onOpenFilters)
            }
        }
        Spacer(Modifier.height(SpaceXl))
        LazyColumn(
            contentPadding = PaddingValues(start = SpaceXl, end = SpaceXl, bottom = Space2xl),
            verticalArrangement = Arrangement.spacedBy(SpaceLg),
        ) {
            item {
                Text(
                    text = "${state.resultCount} RICETTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.results, key = { it.recipe.id }) { item ->
                RecipeListCard(
                    item = item,
                    isFavorite = item.recipe.id in state.favoriteIds,
                    onToggleFavorite = onToggleFavorite,
                    onClick = { onRecipeClick(item.recipe.id) },
                )
            }
        }
    }
}

// -- 2. Filter menu --

@Composable
private fun FiltersContent(
    state: EsploraUiState,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onConsumeSearchFocus: () -> Unit,
    onOpenIngredientSearch: () -> Unit,
    onRemoveIngredient: (Ingredient) -> Unit,
    onOpenCategorySearch: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onToggleMealType: (MealType) -> Unit,
    onToggleTimeWindow: (TimeWindow) -> Unit,
    onToggleDifficulty: (Difficulty) -> Unit,
    onReset: () -> Unit,
    onShowResults: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.requestSearchFocus) {
        if (state.requestSearchFocus) {
            focusRequester.requestFocus()
            onConsumeSearchFocus()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top bar: back + recipe query field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SpaceMd, end = SpaceXl, top = Space2xl, bottom = SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                placeholder = { Text("Cerca ricette...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancella", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(RoundedFull),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = SpaceXl, end = SpaceXl, top = SpaceMd, bottom = SpaceXl),
        ) {
            // Ingredients
            item {
                SectionHeader(
                    title = "Cerca per ingredienti",
                    icon = Icons.Outlined.Restaurant,
                    linkText = "+ Aggiungi",
                    onLink = onOpenIngredientSearch,
                )
                Spacer(Modifier.height(SpaceMd))
                if (state.selectedIngredients.isEmpty()) {
                    EmptyHint("Nessun ingrediente selezionato")
                } else {
                    ChipFlow {
                        state.selectedIngredients.forEach { ingredient ->
                            RemovableChip(label = ingredient.name, onClick = { onRemoveIngredient(ingredient) })
                        }
                    }
                }
                Spacer(Modifier.height(Space2xl))
            }

            // Meal type
            item {
                SectionHeader(title = "Tipo di pasto")
                Spacer(Modifier.height(SpaceMd))
                ChipFlow {
                    MealType.entries.forEach { mealType ->
                        FilterChip(
                            label = mealType.displayLabel,
                            selected = state.mealType == mealType,
                            onClick = { onToggleMealType(mealType) },
                        )
                    }
                }
                Spacer(Modifier.height(Space2xl))
            }

            // Categories
            item {
                SectionHeader(
                    title = "Categorie",
                    linkText = "Sfoglia",
                    onLink = onOpenCategorySearch,
                )
                Spacer(Modifier.height(SpaceMd))
                if (state.selectedCategories.isEmpty()) {
                    EmptyHint("Nessuna categoria selezionata")
                } else {
                    ChipFlow {
                        state.selectedCategories.forEach { category ->
                            RemovableChip(label = category, onClick = { onRemoveCategory(category) })
                        }
                    }
                }
                Spacer(Modifier.height(Space2xl))
            }

            // Time
            item {
                SectionHeader(title = "Tempo", icon = Icons.Outlined.Schedule)
                Spacer(Modifier.height(SpaceMd))
                ChipFlow {
                    TimeWindow.entries.forEach { window ->
                        FilterChip(
                            label = window.label,
                            selected = window in state.timeWindows,
                            onClick = { onToggleTimeWindow(window) },
                        )
                    }
                }
                Spacer(Modifier.height(Space2xl))
            }

            // Difficulty
            item {
                SectionHeader(title = "Difficolta")
                Spacer(Modifier.height(SpaceMd))
                ChipFlow {
                    Difficulty.entries.forEach { difficulty ->
                        FilterChip(
                            label = difficulty.displayLabel,
                            selected = difficulty in state.difficulties,
                            onClick = { onToggleDifficulty(difficulty) },
                        )
                    }
                }
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = SpaceXl, vertical = SpaceLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceLg),
        ) {
            if (state.appliedFilterCount > 0) {
                TextButton(onClick = onReset) {
                    Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = onShowResults,
                shape = RoundedCornerShape(RoundedFull),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            ) {
                val label = if (state.appliedFilterCount > 0) {
                    "Mostra risultati (${state.appliedFilterCount})"
                } else {
                    "Mostra risultati"
                }
                Text(text = label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// -- 3. Ingredient search sub-page --

@Composable
private fun IngredientSearchContent(
    state: EsploraUiState,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onAddSuggestion: (Ingredient) -> Unit,
    onTogglePantry: (Ingredient) -> Unit,
) {
    val selectedIds = state.selectedIngredients.map { it.id }.toSet()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SearchTopBar(
            query = state.ingredientQuery,
            placeholder = "Cerca ingrediente...",
            onBack = onBack,
            onQueryChanged = onQueryChanged,
        )
        LazyColumn(contentPadding = PaddingValues(horizontal = SpaceXl, vertical = SpaceMd)) {
            if (state.ingredientQuery.isNotBlank() && state.ingredientSuggestions.isNotEmpty()) {
                item { OverlineLabel("Suggerimenti") }
                items(state.ingredientSuggestions, key = { "sug-${it.id}" }) { ingredient ->
                    SelectableRow(name = ingredient.name, selected = false, onClick = { onAddSuggestion(ingredient) })
                }
                item { Spacer(Modifier.height(SpaceXl)) }
            }
            if (state.pantryIngredients.isNotEmpty()) {
                item {
                    Text(
                        text = "Dalla tua dispensa",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = SpaceMd),
                    )
                }
                items(state.pantryIngredients, key = { "pan-${it.id}" }) { ingredient ->
                    SelectableRow(
                        name = ingredient.name,
                        selected = ingredient.id in selectedIds,
                        onClick = { onTogglePantry(ingredient) },
                        card = true,
                    )
                }
            }
        }
    }
}

// -- 4. Category search sub-page --

@Composable
private fun CategorySearchContent(
    state: EsploraUiState,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggle: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SearchTopBar(
            query = state.categoryQuery,
            placeholder = "Cerca categoria...",
            onBack = onBack,
            onQueryChanged = onQueryChanged,
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = SpaceXl, vertical = SpaceMd),
            verticalArrangement = Arrangement.spacedBy(SpaceMd),
        ) {
            items(state.visibleCategories, key = { it }) { category ->
                SelectableRow(
                    name = category,
                    selected = category in state.selectedCategories,
                    onClick = { onToggle(category) },
                    card = true,
                )
            }
        }
    }
}

// -- shared building blocks --

@Composable
private fun FakeSearchBar(
    query: String,
    placeholder: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasQuery = query.isNotBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RoundedFull))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(RoundedFull))
            .clickable(onClick = onClick)
            .padding(start = SpaceXl, end = SpaceMd, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(SpaceLg))
        Text(
            text = if (hasQuery) query else placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasQuery) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (hasQuery) {
            IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancella",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Spacer(Modifier.width(SpaceMd))
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    placeholder: String,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SpaceMd, end = SpaceXl, top = Space2xl, bottom = SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancella", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(RoundedFull),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
    linkText: String? = null,
    onLink: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(SpaceMd))
        }
        Text(
            text = title,
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (linkText != null && onLink != null) {
            Text(
                text = linkText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onLink),
            )
        }
    }
}

@Composable
private fun OverlineLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = SpaceMd),
    )
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SpaceMd),
        verticalArrangement = Arrangement.spacedBy(SpaceMd),
        content = content,
    )
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val foreground = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(RoundedFull))
            .background(background)
            .then(
                if (selected) Modifier
                else Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(RoundedFull),
                ),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = SpaceXl, vertical = SpaceMd),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = foreground)
    }
}

/** Selected filter pill (orange). Clicking it removes the filter. */
@Composable
private fun RemovableChip(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(RoundedFull))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 10.dp, top = SpaceMd, bottom = SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Rimuovi",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SelectableRow(name: String, selected: Boolean, onClick: () -> Unit, card: Boolean = false) {
    val base = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
    val styled = if (card) {
        val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface
        base
            .clip(RoundedCornerShape(RoundedFull))
            .background(bg)
            .padding(horizontal = SpaceXl, vertical = 14.dp)
    } else {
        base.padding(vertical = SpaceLg)
    }
    Row(modifier = styled, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = if (selected) Icons.Default.Check else Icons.Default.Add,
            contentDescription = if (selected) "Selezionato" else "Aggiungi",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}
