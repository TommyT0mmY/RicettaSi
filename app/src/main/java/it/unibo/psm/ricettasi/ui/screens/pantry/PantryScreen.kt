package it.unibo.psm.ricettasi.ui.screens.pantry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.domain.model.PantryItem
import it.unibo.psm.ricettasi.ui.theme.CardElevation
import it.unibo.psm.ricettasi.ui.theme.RoundedFull
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import it.unibo.psm.ricettasi.ui.theme.SpaceXs
import it.unibo.psm.ricettasi.ui.theme.customColors
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// -- Route --

/** Connects [PantryViewModel] to the UI, forwarding every user action. */
@Composable
fun PantryRoute(
    viewModel: PantryViewModel = koinViewModel(),
) {
    val activeItems by viewModel.activeItems.collectAsStateWithLifecycle()
    val consumedItems by viewModel.consumedItems.collectAsStateWithLifecycle()
    val isFinishedExpanded by viewModel.isFinishedExpanded.collectAsStateWithLifecycle()
    val addSheet by viewModel.addSheet.collectAsStateWithLifecycle()

    PantryScreen(
        activeItems = activeItems,
        consumedItems = consumedItems,
        isFinishedExpanded = isFinishedExpanded,
        addSheet = addSheet,
        onToggleFinishedExpanded = viewModel::toggleFinishedExpanded,
        onToggleConsumed = viewModel::toggleConsumed,
        onDeleteItem = viewModel::deleteItem,
        onRestoreItem = viewModel::restoreItem,
        onShowAddSheet = viewModel::showAddSheet,
        onHideAddSheet = viewModel::hideAddSheet,
        onNameQueryChanged = viewModel::onNameQueryChanged,
        onSuggestionSelected = viewModel::onSuggestionSelected,
        onQuantityChanged = viewModel::onQuantityChanged,
        onExpiryDateSelected = viewModel::onExpiryDateSelected,
        onClearExpiryDate = viewModel::onClearExpiryDate,
        onAddIngredient = viewModel::addIngredient,
    )
}

// -- Main screen --

/**
 * Pantry tab: active ingredients with expiry info, a collapsible
 * "Articoli finiti" section, a FAB that opens the add-ingredient bottom sheet,
 * and a trash icon on every card for deletion (with undo via snackbar).
 */
@Composable
private fun PantryScreen(
    activeItems: List<PantryItemDisplay>,
    consumedItems: List<PantryItemDisplay>,
    isFinishedExpanded: Boolean,
    addSheet: AddIngredientState,
    onToggleFinishedExpanded: () -> Unit,
    onToggleConsumed: (id: String, currentlyConsumed: Boolean) -> Unit,
    onDeleteItem: (id: String) -> Unit,
    onRestoreItem: (item: PantryItem) -> Unit,
    onShowAddSheet: () -> Unit,
    onHideAddSheet: () -> Unit,
    onNameQueryChanged: (String) -> Unit,
    onSuggestionSelected: (it.unibo.psm.ricettasi.domain.model.Ingredient) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onExpiryDateSelected: (java.time.LocalDate) -> Unit,
    onClearExpiryDate: () -> Unit,
    onAddIngredient: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (activeItems.isEmpty() && consumedItems.isEmpty()) {
            EmptyPantry(onAdd = onShowAddSheet)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                item { PantryHeader(activeCount = activeItems.size) }

                items(activeItems, key = { it.pantryItem.id }) { item ->
                    val deleteWithUndo: () -> Unit = {
                        val deleted = item.pantryItem
                        onDeleteItem(deleted.id)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${item.ingredientName} rimosso",
                                actionLabel = "Annulla",
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreItem(deleted)
                            }
                        }
                    }
                    PantryItemCard(
                        item = item,
                        isConsumed = false,
                        onConsumedToggle = { onToggleConsumed(item.pantryItem.id, false) },
                        onDelete = deleteWithUndo,
                    )
                }

                if (consumedItems.isNotEmpty()) {
                    item {
                        FinishedItemsHeader(
                            count = consumedItems.size,
                            isExpanded = isFinishedExpanded,
                            onToggle = onToggleFinishedExpanded,
                        )
                    }

                    if (isFinishedExpanded) {
                        items(consumedItems, key = { it.pantryItem.id }) { item ->
                            val deleteWithUndo: () -> Unit = {
                                val deleted = item.pantryItem
                                onDeleteItem(deleted.id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${item.ingredientName} rimosso",
                                        actionLabel = "Annulla",
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onRestoreItem(deleted)
                                    }
                                }
                            }
                            PantryItemCard(
                                item = item,
                                isConsumed = true,
                                onConsumedToggle = { onToggleConsumed(item.pantryItem.id, true) },
                                onDelete = deleteWithUndo,
                            )
                        }
                    }
                }
            }
        }

        // Snackbar sits above the FAB, always at the bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp),
        )

        // FAB anchored low, close to the bottom bar.
        // Hidden while the user scrolls down so it does not cover content.
        AnimatedVisibility(
            visible = listState.firstVisibleItemIndex == 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            FloatingActionButton(
                onClick = onShowAddSheet,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(end = SpaceXl, bottom = SpaceMd),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi ingrediente")
            }
        }
    }

    // -- Add ingredient bottom sheet --
    if (addSheet.isVisible) {
        AddIngredientSheet(
            state = addSheet,
            onDismiss = onHideAddSheet,
            onNameQueryChanged = onNameQueryChanged,
            onSuggestionSelected = onSuggestionSelected,
            onQuantityChanged = onQuantityChanged,
            onExpiryDateSelected = onExpiryDateSelected,
            onClearExpiryDate = onClearExpiryDate,
            onAdd = onAddIngredient,
        )
    }
}

// -- Header --

/** Page title: "Dispensa" + ingredient count subtitle. */
@Composable
private fun PantryHeader(activeCount: Int) {
    Column(
        modifier = Modifier.padding(start = SpaceXl, end = SpaceXl, top = Space2xl, bottom = SpaceMd),
    ) {
        Text(
            text = "Dispensa",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(SpaceXs))
        Text(
            text = "$activeCount ingredienti",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// -- Empty state --

/** Shown when the pantry has no items at all. */
@Composable
private fun EmptyPantry(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "La dispensa é vuota",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SpaceMd))
        Text(
            text = "Aggiungi i tuoi ingredienti per scoprire le ricette che puoi cucinare.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Space2xl))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(RoundedFull),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Aggiungi ingrediente")
        }
    }
}

// -- Single pantry card --

/**
 * A card for one pantry item. Shows:
 * - left: a custom checkbox (empty circle for active, filled orange with check for consumed)
 * - center: ingredient name + quantity + expiry status dot
 * - right: trash icon for hard-delete
 */
@Composable
private fun PantryItemCard(
    item: PantryItemDisplay,
    isConsumed: Boolean,
    onConsumedToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = SpaceXl, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: custom checkbox
            PantryCheckbox(
                checked = isConsumed,
                onClick = onConsumedToggle,
            )

            Spacer(Modifier.width(SpaceLg))

            // Center: name + quantity + expiry
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.ingredientName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isConsumed) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                if (isConsumed) {
                    Text(
                        text = "finito",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    item.pantryItem.quantity?.let { qty ->
                        Text(
                            text = qty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ExpiryLabel(item)
                }
            }

            // Right: trash icon -- hard-deletes the item (with undo via snackbar)
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Elimina ${item.ingredientName}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onDelete),
            )
        }
    }
}

// -- Custom checkbox --

/** Circular toggle: empty circle when unchecked, filled orange with white check when checked. */
@Composable
private fun PantryCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .size(Space2xl)
            .then(
                if (checked) {
                    Modifier
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                        .clip(shape)
                        .border(SpaceXs, MaterialTheme.colorScheme.onSurfaceVariant, shape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Spunta come attivo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(SpaceXl),
            )
        }
    }
}

// -- Expiry dot + label --

/** Coloured dot and text showing how many days until expiry. */
@Composable
private fun ExpiryLabel(item: PantryItemDisplay) {
    val status = item.expiryStatus ?: return
    val days = item.daysUntilExpiry

    val color = when (status) {
        it.unibo.psm.ricettasi.domain.model.ExpiryStatus.CRITICAL -> MaterialTheme.customColors.statusCritical
        it.unibo.psm.ricettasi.domain.model.ExpiryStatus.WARNING -> MaterialTheme.customColors.statusWarning
        it.unibo.psm.ricettasi.domain.model.ExpiryStatus.SECONDARY -> MaterialTheme.customColors.statusOk
        it.unibo.psm.ricettasi.domain.model.ExpiryStatus.OK -> MaterialTheme.customColors.statusOk
    }

    val label = when {
        days == null -> return
        days < 0 -> "Scaduto"
        days == 0L -> "Scade oggi"
        days == 1L -> "Scade domani"
        else -> "Tra $days giorni"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(SpaceMd)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

// -- "Articoli finiti" section header --

/** Section header for consumed items: "Articoli finiti . N" with a chevron. */
@Composable
private fun FinishedItemsHeader(
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = SpaceXl, vertical = SpaceLg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Articoli finiti · $count",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = if (isExpanded) {
                Icons.Default.KeyboardArrowUp
            } else {
                Icons.Default.KeyboardArrowDown
            },
            contentDescription = if (isExpanded) "Nascondi" else "Mostra",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
