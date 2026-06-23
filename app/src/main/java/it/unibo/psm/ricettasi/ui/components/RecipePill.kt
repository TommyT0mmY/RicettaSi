package it.unibo.psm.ricettasi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.unibo.psm.ricettasi.ui.theme.RoundedFull
import it.unibo.psm.ricettasi.ui.theme.SpaceLg

/** Visual style for a [RecipePill]. */
enum class PillStyle {
    /** Category pills: light orange background, accent orange text. */
    CATEGORY,
    /** Meal-type pills: light grey background, secondary grey text. */
    MEAL_TYPE,
}

private val CategoryBg = Color(0xFFFFF0EB)
private val CategoryText = Color(0xFFE65F2B)
private val MealTypeBg = Color(0xFFF5F0EB)
private val MealTypeText = Color(0xFF8C827A)

/**
 * A small rounded tag used to display recipe categories and meal types.
 *
 * Two styles are supported: [PillStyle.CATEGORY] for category tags (orange-on-cream)
 * and [PillStyle.MEAL_TYPE] for meal-type tags (grey-on-cream).
 */
@Composable
fun RecipePill(
    text: String,
    style: PillStyle,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (style) {
        PillStyle.CATEGORY -> CategoryBg to CategoryText
        PillStyle.MEAL_TYPE -> MealTypeBg to MealTypeText
    }

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(RoundedFull))
            .background(bgColor)
            .padding(horizontal = SpaceLg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}