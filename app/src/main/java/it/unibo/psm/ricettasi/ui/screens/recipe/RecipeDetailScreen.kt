 package it.unibo.psm.ricettasi.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.MealType
import it.unibo.psm.ricettasi.domain.model.Recipe
import it.unibo.psm.ricettasi.domain.model.RecipeIngredient
import it.unibo.psm.ricettasi.ui.components.PillStyle
import it.unibo.psm.ricettasi.ui.components.RecipePill
import it.unibo.psm.ricettasi.ui.theme.CardElevation
import it.unibo.psm.ricettasi.ui.theme.FrauncesFamily
import it.unibo.psm.ricettasi.ui.theme.IconBtn
import it.unibo.psm.ricettasi.ui.theme.IconSm
import it.unibo.psm.ricettasi.ui.theme.ManropeFamily
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.Space2xl
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceSm
import it.unibo.psm.ricettasi.ui.theme.SpaceXl
import it.unibo.psm.ricettasi.ui.theme.SpaceXs
import org.koin.androidx.compose.koinViewModel

/**
 * Scale a quantity string by a multiplier.
 *
 * Extracts a leading integer from strings like "160g" or "2 cucchiai" and
 * multiplies it. Strings without a leading integer (e.g. "q.b.") are
 * returned as-is.
 */
internal fun scaleQuantity(quantity: String, multiplier: Int): String {
    if (multiplier == 1) return quantity
    val match = Regex("^(\\d+)(.*)").find(quantity.trim())
    if (match != null) {
        val number = match.groupValues[1].toInt()
        val suffix = match.groupValues[2]
        return "${number * multiplier}$suffix"
    }
    return quantity
}

// -- Colours used locally --

private val AccentColor = Color(0xFFE65F2B)
private val AccentDark = Color(0xFFFF8A58)
private val WarmBlack = Color(0xFF2A2421)
private val SecondaryGrey = Color(0xFF8C827A)
private val OverlayCircleBg = Color.White.copy(alpha = 0.85f)

// -- Route --

@Composable
fun RecipeDetailRoute(
    recipeId: String,
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(recipeId) {
        viewModel.init(recipeId)
    }

    RecipeDetailScreen(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onMarkCooked = viewModel::markCooked,
        onShare = { viewModel.share(context) },
    )
}

// -- Main screen --

@Composable
private fun RecipeDetailScreen(
    state: RecipeDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkCooked: () -> Unit,
    onShare: () -> Unit,
) {
    when {
        state.isLoading && state.recipe == null -> LoadingContent()
        state.errorMessage != null && state.recipe == null -> ErrorContent(
            message = state.errorMessage!!,
            onBack = onBack,
        )
        else -> RecipeContent(
            state = state,
            onBack = onBack,
            onToggleFavorite = onToggleFavorite,
            onMarkCooked = onMarkCooked,
            onShare = onShare,
        )
    }
}

// -- Loading / error states --

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AccentColor)
    }
}

@Composable
private fun ErrorContent(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryGrey,
        )
        Spacer(Modifier.height(SpaceLg))
        Text(
            text = "Torna indietro",
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = AccentColor,
            modifier = Modifier.clickable(onClick = onBack),
        )
    }
}

// -- Full recipe content --

@Composable
private fun RecipeContent(
    state: RecipeDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkCooked: () -> Unit,
    onShare: () -> Unit,
) {
    val recipe = state.recipe ?: return
    var selectedTab by remember { mutableIntStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val heroHeight = maxHeight * 0.38f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 1. Hero image with overlay
            HeroSection(
                imageUrl = recipe.imageUrl,
                title = recipe.title,
                height = heroHeight,
                isFavorite = state.isFavorite,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
                onShare = onShare,
            )

            Spacer(Modifier.height(SpaceLg))

            // 2. Tag pills
            if (recipe.categories.isNotEmpty() || recipe.mealTypes.isNotEmpty()) {
                TagRow(
                    categories = recipe.categories,
                    mealTypes = recipe.mealTypes,
                )
                Spacer(Modifier.height(SpaceLg))
            }

            // 3. Title
            Text(
                text = recipe.title,
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 33.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = SpaceXl),
            )

            Spacer(Modifier.height(SpaceLg))

            // 4. Metadata card
            MetadataCard(
                preparationTime = recipe.preparationTime,
                difficulty = recipe.difficulty,
            )

            Spacer(Modifier.height(Space2xl))

            // 5. Tab selector
            TabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )

            Spacer(Modifier.height(Space2xl))

            // 6. Tab content
            when (selectedTab) {
                0 -> IngredientsTab(
                    ingredients = recipe.ingredients,
                    ingredientNames = state.ingredientNames,
                )
                1 -> PreparationTab(
                    steps = recipe.steps,
                    isCooked = state.isCooked,
                    onMarkCooked = onMarkCooked,
                )
            }

            // Bottom spacing so content isn't clipped by the nav bar
            Spacer(Modifier.height(Space2xl))
        }
    }
}

// -- 1. Hero image + overlay --

@Composable
private fun HeroSection(
    imageUrl: String?,
    title: String,
    height: androidx.compose.ui.unit.Dp,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0D8D0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.firstOrNull()?.uppercase() ?: "R",
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }

        // Overlay icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpaceXl, start = SpaceXl, end = SpaceXl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(IconBtn)
                    .clip(CircleShape)
                    .background(OverlayCircleBg),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = WarmBlack,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Share + Favourite
            Row(horizontalArrangement = Arrangement.spacedBy(SpaceSm)) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(IconBtn)
                        .clip(CircleShape)
                        .background(OverlayCircleBg),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Condividi",
                        tint = WarmBlack,
                        modifier = Modifier.size(20.dp),
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(IconBtn)
                        .clip(CircleShape)
                        .background(OverlayCircleBg),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Preferiti",
                        tint = if (isFavorite) AccentColor else WarmBlack,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// -- 2. Tag pills row --

@Composable
private fun TagRow(
    categories: List<String>,
    mealTypes: List<MealType>,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = SpaceXl),
        horizontalArrangement = Arrangement.spacedBy(SpaceMd),
    ) {
        // Categories first (orange style)
        categories.forEach { category ->
            item(key = "cat-$category") {
                RecipePill(text = category, style = PillStyle.CATEGORY)
            }
        }
        // Meal types after (grey style)
        mealTypes.forEach { mealType ->
            item(key = "meal-${mealType.value}") {
                RecipePill(text = mealType.value.replaceFirstChar { it.uppercase() }, style = PillStyle.MEAL_TYPE)
            }
        }
    }
}

// -- 4. Metadata card --

@Composable
private fun MetadataCard(
    preparationTime: Int?,
    difficulty: Difficulty,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpaceXl),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Prep time
            MetadataColumn(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Tempo",
                        tint = AccentColor,
                        modifier = Modifier.size(IconSm),
                    )
                },
                value = if (preparationTime != null) "$preparationTime min" else "--",
                label = "Preparazione",
            )

            // Difficulty
            MetadataColumn(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Difficolta'",
                        tint = AccentColor,
                        modifier = Modifier.size(IconSm),
                    )
                },
                value = difficulty.value.replaceFirstChar { it.uppercase() },
                label = "Difficolta'",
            )
        }
    }
}

@Composable
private fun MetadataColumn(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Spacer(Modifier.height(SpaceXs))
        Text(
            text = value,
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = SecondaryGrey,
        )
    }
}

// -- 5. Tab selector --

@Composable
private fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpaceXl),
        ) {
            TabItem(
                text = "Ingredienti",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f),
            )
            TabItem(
                text = "Preparazione",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f),
            )
        }
        // Bottom rule with accent highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpaceXs)
                .height(3.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(3.dp)
                    .then(
                        if (selectedTab == 0) Modifier.align(Alignment.CenterStart)
                        else Modifier.align(Alignment.CenterEnd)
                    )
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentColor),
            )
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontFamily = ManropeFamily,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 16.sp,
        color = if (isSelected) MaterialTheme.colorScheme.onBackground else SecondaryGrey,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = modifier.clickable(onClick = onClick),
    )
}

// -- 6a. Ingredients tab --

@Composable
private fun IngredientsTab(
    ingredients: List<RecipeIngredient>,
    ingredientNames: Map<String, String>,
) {
    var servings by remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl),
    ) {
        // Serving stepper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Ingredienti per",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = SecondaryGrey,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Minus
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .clickable { if (servings > 1) servings-- },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "-",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.width(SpaceLg))

                Text(
                    text = "$servings",
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.width(SpaceLg))

                // Plus
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .clickable { servings++ },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(Modifier.height(Space2xl))

        // Ingredient rows
        if (ingredients.isEmpty()) {
            Text(
                text = "Nessun ingrediente",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SecondaryGrey,
                modifier = Modifier.padding(vertical = SpaceLg),
            )
        } else {
            ingredients.forEachIndexed { index, ingredient ->
                val name = ingredient.name
                    ?: ingredientNames[ingredient.ingredientId]
                    ?: ingredient.ingredientId
                val quantity = ingredient.quantity?.let { scaleQuantity(it, servings) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpaceMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = name,
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    if (quantity != null) {
                        Text(
                            text = quantity,
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AccentColor,
                        )
                    }
                }

                // Divider between items
                if (index < ingredients.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    )
                }
            }
        }
    }
}

// -- 6b. Preparation tab --

@Composable
private fun PreparationTab(
    steps: List<String>,
    isCooked: Boolean,
    onMarkCooked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceXl),
        verticalArrangement = Arrangement.spacedBy(SpaceLg),
    ) {
        if (steps.isEmpty()) {
            Text(
                text = "Nessun passaggio",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SecondaryGrey,
                modifier = Modifier.padding(vertical = SpaceLg),
            )
        } else {
            steps.forEachIndexed { index, step ->
                StepCard(stepNumber = index + 1, text = step)
            }
        }

        // "Cucinato" button — appears after the last step
        Spacer(Modifier.height(SpaceMd))
        Button(
            onClick = onMarkCooked,
            enabled = !isCooked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(RoundedLg),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCooked) SecondaryGrey else AccentColor,
                disabledContainerColor = SecondaryGrey.copy(alpha = 0.3f),
            ),
        ) {
            if (isCooked) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(IconSm),
                )
                Spacer(Modifier.width(SpaceSm))
            }
            Text(
                text = if (isCooked) "Gia' cucinata" else "L'ho cucinata!",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black,
            )
        }
    }
}

@Composable
private fun StepCard(stepNumber: Int, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceXl),
            horizontalArrangement = Arrangement.spacedBy(SpaceLg),
            verticalAlignment = Alignment.Top,
        ) {
            // Numbered circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = String.format("%02d", stepNumber),
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                )
            }

            // Step description
            Text(
                text = text,
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}