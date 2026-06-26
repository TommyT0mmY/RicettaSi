package it.unibo.psm.ricettasi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.psm.ricettasi.domain.model.Badge
import it.unibo.psm.ricettasi.ui.theme.SpaceMd
import it.unibo.psm.ricettasi.ui.theme.SpaceSm

/**
 * Resolves a badge [code] to its bundled vector drawable resource ID.
 *
 * Each badge has its own custom icon under `res/drawable/badge_<code>.xml`.
 * If the code does not match any known drawable a generic fallback is used.
 */
private fun badgeDrawableRes(code: String): Int = when (code) {
    "primo-piatto"       -> it.unibo.psm.ricettasi.R.drawable.badge_primo_piatto
    "cuoco-abituale"     -> it.unibo.psm.ricettasi.R.drawable.badge_cuoco_abituale
    "repertorio-vario"   -> it.unibo.psm.ricettasi.R.drawable.badge_repertorio_vario
    "esploratore"        -> it.unibo.psm.ricettasi.R.drawable.badge_esploratore
    "dispensa-piena"     -> it.unibo.psm.ricettasi.R.drawable.badge_dispensa_piena
    "collezionista"      -> it.unibo.psm.ricettasi.R.drawable.badge_collezionista
    "esperto"            -> it.unibo.psm.ricettasi.R.drawable.badge_esperto
    "maestro"            -> it.unibo.psm.ricettasi.R.drawable.badge_maestro
    "salva-cibo"         -> it.unibo.psm.ricettasi.R.drawable.badge_salva_cibo
    "gufo-fornelli"      -> it.unibo.psm.ricettasi.R.drawable.badge_gufo_fornelli
    "cucina-lampo"       -> it.unibo.psm.ricettasi.R.drawable.badge_cucina_lampo
    "colazione-campioni" -> it.unibo.psm.ricettasi.R.drawable.badge_colazione_campioni
    else                 -> it.unibo.psm.ricettasi.R.drawable.badge_fallback
}

/** A single badge icon, sized for grids and lists. */
@Composable
fun BadgeIcon(
    badge: Badge,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val grayscaleMatrix = remember {
        ColorMatrix().apply { setToSaturation(0f) }
    }

    Image(
        painter = painterResource(id = badgeDrawableRes(badge.code)),
        contentDescription = badge.name,
        colorFilter = if (unlocked) null else ColorFilter.colorMatrix(grayscaleMatrix),
        alpha = if (unlocked) 1f else 0.45f,
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
