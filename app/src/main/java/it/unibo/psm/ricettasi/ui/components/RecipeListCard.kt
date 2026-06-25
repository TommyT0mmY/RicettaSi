package it.unibo.psm.ricettasi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import it.unibo.psm.ricettasi.ui.theme.CardElevation
import it.unibo.psm.ricettasi.ui.theme.CardImg
import it.unibo.psm.ricettasi.ui.theme.RoundedFull
import it.unibo.psm.ricettasi.ui.theme.RoundedLg
import it.unibo.psm.ricettasi.ui.theme.RoundedMd
import it.unibo.psm.ricettasi.ui.theme.SpaceLg
import it.unibo.psm.ricettasi.ui.theme.SpaceSm
import it.unibo.psm.ricettasi.ui.theme.SpaceXs
import it.unibo.psm.ricettasi.ui.theme.customColors

/** Italian label shown for a recipe difficulty on the cards. */
internal val Difficulty.displayLabel: String
    get() = when (this) {
        Difficulty.FACILE -> "Facile"
        Difficulty.MEDIO -> "Media"
        Difficulty.DIFFICILE -> "Difficile"
    }

/**
 * Standard recipe card used in the vertical lists (Home, Svuota il frigo, Esplora):
 * square image on the left, title and metadata on the right, favourite heart top-right
 * and the match/expiring badges below.
 */
@Composable
fun RecipeListCard(
    item: RecipeWithAvailability,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = item.recipe
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundedLg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceLg),
            verticalAlignment = Alignment.Top,
        ) {
            RecipeImage(
                imageUrl = summary.imageUrl,
                modifier = Modifier
                    .size(CardImg)
                    .clip(RoundedCornerShape(RoundedMd)),
            )
            Spacer(Modifier.width(SpaceLg))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = summary.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(SpaceSm))
                    IconButton(
                        onClick = { onToggleFavorite(summary.id) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                val meta = buildList {
                    if (summary.preparationTime != null) add("${summary.preparationTime} min")
                    add(summary.difficulty.displayLabel)
                    summary.categories.firstOrNull()?.let { add(it) }
                }.joinToString(" · ")
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.matchPercent > 0) {
                        MatchBadge(percent = item.matchPercent)
                    }
                    if (item.expiringMatchCount > 0) {
                        LeafBadge(count = item.expiringMatchCount)
                    }
                }
            }
        }
    }
}

/** Recipe image with an emoji-free placeholder while there is no url. */
@Composable
fun RecipeImage(imageUrl: String?, modifier: Modifier = Modifier) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MatchBadge(percent: Int) {
    val bgColor = when {
        percent >= 80 -> MaterialTheme.customColors.statusOk.copy(alpha = 0.15f)
        percent >= 50 -> MaterialTheme.customColors.statusWarning.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    }
    val fgColor = when {
        percent >= 80 -> MaterialTheme.customColors.statusOk
        percent >= 50 -> MaterialTheme.customColors.statusWarning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(RoundedFull))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = SpaceXs),
    ) {
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall,
            color = fgColor,
        )
    }
}

@Composable
private fun LeafBadge(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(RoundedFull))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = SpaceXs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceXs),
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(10.dp),
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
