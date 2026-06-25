package it.unibo.psm.ricettasi.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** Navigation route constants. The first 5 are tabs; the rest are push destinations. */
object Routes {
    const val HOME = "home"
    const val PANTRY = "pantry"
    const val ESPLORA = "esplora"
    const val PREFERITI = "preferiti"
    const val PROFILO = "profilo"
    const val SETTINGS = "settings"

    const val RECIPE_DETAIL = "recipe/{recipeId}"
    fun recipeDetail(recipeId: String) = "recipe/$recipeId"

    const val SVUOTA_IL_FRIGO = "svuota_il_frigo"
}

/** Bottom bar entry: route, label and icon. */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Outlined.Home),
    BottomNavItem(Routes.PANTRY, "Dispensa", Icons.Outlined.Kitchen),
    BottomNavItem(Routes.ESPLORA, "Esplora", Icons.Outlined.Explore),
    BottomNavItem(Routes.PREFERITI, "Preferiti", Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.PROFILO, "Profilo", Icons.Outlined.Person),
)
