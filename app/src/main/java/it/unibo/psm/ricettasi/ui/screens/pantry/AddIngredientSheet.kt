package it.unibo.psm.ricettasi.ui.screens.pantry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import it.unibo.psm.ricettasi.domain.model.Ingredient
import it.unibo.psm.ricettasi.ui.theme.RoundedFull
import it.unibo.psm.ricettasi.ui.theme.RoundedMd
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceSm
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Modal bottom sheet for adding a new ingredient to the pantry.
 *
 * Contains a name field with autocomplete (searching the ingredient list),
 * an optional quantity field, an optional date picker for expiry, and the submit button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientSheet(
    state: AddIngredientState,
    onDismiss: () -> Unit,
    onNameQueryChanged: (String) -> Unit,
    onSuggestionSelected: (Ingredient) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onExpiryDateSelected: (LocalDate) -> Unit,
    onClearExpiryDate: () -> Unit,
    onAdd: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = Space2xl, topEnd = Space2xl),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space2xl)
                .padding(bottom = 32.dp)
                .imePadding(),
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Nuovo ingrediente",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Chiudi",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(Space2xl)
                        .clickable(onClick = onDismiss),
                )
            }

            Spacer(Modifier.height(Space2xl))

            // -- Name field with autocomplete --
            Text(
                text = "Nome",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SpaceSm))
            IngredientAutocompleteField(
                query = state.nameQuery,
                suggestions = state.suggestions,
                isSearching = state.isSearching,
                onQueryChanged = onNameQueryChanged,
                onSuggestionSelected = onSuggestionSelected,
            )

            Spacer(Modifier.height(SpaceXl))

            // -- Quantity field --
            Text(
                text = "Quantita'",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SpaceSm))
            OutlinedTextField(
                value = state.quantity,
                onValueChange = onQuantityChanged,
                placeholder = {
                    Text(
                        "es. 200 g, 3 pezzi",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(RoundedMd),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )

            Spacer(Modifier.height(SpaceXl))

            // -- Expiry date field --
            Text(
                text = "Data di scadenza",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SpaceSm))
            ExpiryDateField(
                date = state.expiryDate,
                onDateSelected = onExpiryDateSelected,
                onClearDate = onClearExpiryDate,
            )

            // -- Error message --
            state.errorMessage?.let { error ->
                Spacer(Modifier.height(SpaceXl))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(Space2xl))

            // -- Submit button --
            Button(
                onClick = onAdd,
                enabled = !state.isSubmitting && state.nameQuery.isNotBlank(),
                shape = RoundedCornerShape(RoundedFull),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Text(
                        text = "Aggiungi alla dispensa",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

// -- Autocomplete field --

/**
 * Text field that performs a prefix search as the user types and shows
 * matching ingredients in a dropdown that appears automatically below the field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientAutocompleteField(
    query: String,
    suggestions: List<Ingredient>,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onSuggestionSelected: (Ingredient) -> Unit,
) {
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }

    // Auto-expand when suggestions arrive while the query is not blank.
    val showDropdown = suggestions.isNotEmpty() && query.isNotBlank()

    Box {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChanged(it)
                dropdownExpanded = it.isNotBlank()
            },
            placeholder = {
                Text(
                    "es. Pomodori, Farina...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(RoundedMd),
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        )

        DropdownMenu(
            expanded = showDropdown && dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            shape = RoundedCornerShape(RoundedMd),
            // focusable = false so opening the suggestions popup doesn't steal focus from the
            // text field, otherwise the keyboard closes the moment a suggestion shows up.
            properties = PopupProperties(focusable = false),
        ) {
            suggestions.take(5).forEach { ingredient ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = ingredient.name,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        onSuggestionSelected(ingredient)
                        dropdownExpanded = false
                    },
                )
            }
        }
    }
}

// -- Expiry date field --

/** Read-only text field that opens a [DatePickerDialog] on tap.
 * When a date is selected the trailing icon becomes a clear button ("X");
 * otherwise it shows a calendar icon. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpiryDateField(
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onClearDate: () -> Unit,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val displayText = date?.format(formatter) ?: ""

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        placeholder = {
            Text(
                "gg/mm/aaaa",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(RoundedMd),
        trailingIcon = {
            if (date != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Rimuovi data",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onClearDate() },
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "Scegli data",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { showPicker = true },
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { showPicker = true },
    )

    if (showPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selected)
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Annulla")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
