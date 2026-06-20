package it.unibo.psm.ricettasi.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.psm.ricettasi.R
import it.unibo.psm.ricettasi.domain.model.Badge

/**
 * Resolves a badge [code] to its bundled vector drawable.
 *
 * The mapping is explicit (no reflection) so that ProGuard/R8 cannot strip
 * the resources. Unknown codes return the fallback trophy icon.
 */
@DrawableRes
fun badgeDrawableRes(code: String): Int = when (code) {
    "primo-piatto"       -> R.drawable.badge_primo_piatto
    "cuoco-abituale"     -> R.drawable.badge_cuoco_abituale
    "repertorio-vario"   -> R.drawable.badge_repertorio_vario
    "esploratore"        -> R.drawable.badge_esploratore
    "dispensa-piena"     -> R.drawable.badge_dispensa_piena
    "collezionista"      -> R.drawable.badge_collezionista
    "esperto"            -> R.drawable.badge_esperto
    "maestro"            -> R.drawable.badge_maestro
    "salva-cibo"         -> R.drawable.badge_salva_cibo
    "gufo-fornelli"      -> R.drawable.badge_gufo_fornelli
    "cucina-lampo"       -> R.drawable.badge_cucina_lampo
    "colazione-campioni" -> R.drawable.badge_colazione_campioni
    else                 -> R.drawable.badge_fallback
}

/** A single badge icon, sized for grids and lists. */
@Composable
fun BadgeIcon(
    badge: Badge,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val resId = badgeDrawableRes(badge.code)

    Image(
        painter = painterResource(id = resId),
        contentDescription = badge.name,
        modifier = modifier
            .size(size)
            .then(
                if (unlocked) Modifier
                else Modifier.alpha(0.38f)
            ),
        contentScale = ContentScale.Fit,
        colorFilter = if (unlocked) null
        else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
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
        modifier = modifier.padding(8.dp),
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
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
