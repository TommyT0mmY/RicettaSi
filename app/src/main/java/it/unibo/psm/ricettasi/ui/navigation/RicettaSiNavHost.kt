package it.unibo.psm.ricettasi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import it.unibo.psm.ricettasi.ui.screens.home.HomeRoute
import it.unibo.psm.ricettasi.ui.screens.pantry.PantryRoute
import it.unibo.psm.ricettasi.ui.screens.profile.ProfiloRoute
import it.unibo.psm.ricettasi.ui.screens.recipe.RecipeDetailRoute
import it.unibo.psm.ricettasi.ui.screens.settings.SettingsRoute
import org.koin.compose.koinInject

/**
 * Root of the authenticated app: bottom bar with 5 tabs (Home, Dispensa, Esplora,
 * Preferiti, Profilo) and a NavHost that routes between tabs and push destinations.
 */
@Composable
fun RicettaSiNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeRoute(
                    onNavigateToRecipe = { id -> navController.navigate(Routes.recipeDetail(id)) },
                    onNavigateToSvuotaIlFrigo = { navController.navigate(Routes.SVUOTA_IL_FRIGO) },
                    onNavigateToPantry = {
                        navController.navigate(Routes.PANTRY) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.PANTRY) { PantryRoute() }
            composable(Routes.SVUOTA_IL_FRIGO) { TabPlaceholder("Svuota il frigo") }

            composable(Routes.PROFILO) {
                ProfiloRoute(
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsRoute(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
                RecipeDetailRoute(
                    recipeId = recipeId,
                    onBack = { navController.popBackStack() },
                )
            }

            // -- Tab: placeholder (for screens not yet implemented) --
            composable(Routes.ESPLORA) { TabPlaceholder("Esplora") }
            composable(Routes.PREFERITI) { TabPlaceholder("Preferiti") }
        }
    }
}

/** Placeholder for a screen that hasn't been implemented yet. */
@Composable
private fun TabPlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}