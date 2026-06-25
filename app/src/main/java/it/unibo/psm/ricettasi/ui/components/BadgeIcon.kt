package it.unibo.psm.ricettasi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FreeBreakfast
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.psm.ricettasi.domain.model.Badge
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceSm

/**
 * Resolves a badge [code] to its Material icon.
 *
 * The mapping uses the extended Material Icons set, so every icon is
 * available without custom bundled drawables.
 */
internal fun badgeIcon(code: String): ImageVector = when (code) {
    "primo-piatto"       -> Icons.Outlined.Restaurant
    "cuoco-abituale"     -> Icons.Outlined.Star
    "repertorio-vario"   -> Icons.Outlined.AutoAwesome
    "esploratore"        -> Icons.Outlined.TravelExplore
    "dispensa-piena"     -> Icons.Outlined.Inventory2
    "collezionista"      -> Icons.Outlined.CollectionsBookmark
    "esperto"            -> Icons.Outlined.WorkspacePremium
    "maestro"            -> Icons.Outlined.School
    "salva-cibo"         -> Icons.Outlined.Eco
    "gufo-fornelli"      -> Icons.Outlined.NightsStay
    "cucina-lampo"       -> Icons.Outlined.Bolt
    "colazione-campioni" -> Icons.Outlined.FreeBreakfast
    else                 -> Icons.Outlined.EmojiEvents
}

/** A single badge icon, sized for grids and lists. */
@Composable
fun BadgeIcon(
    badge: Badge,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Icon(
        imageVector = badgeIcon(badge.code),
        contentDescription = badge.name,
        tint = if (unlocked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        modifier = modifier.size(size),
    )
}

/**
 * A badge cell for use in the profile grid.
 *
 * Shows the icon, name and locked/unlocked visual state.
 */
@Composable
fun BadgeGridItem(
    badge: Badge,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
) {
    Column(
        modifier = modifier.padding(SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BadgeIcon(
            badge = badge,
            unlocked = unlocked,
            size = iconSize,
        )
        Text(
            text = badge.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = SpaceSm),
        )
    }
}
