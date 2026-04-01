package com.shohiebsense.pokerekom.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shohiebsense.pokerekom.presentation.recommendation.RecommendationScreen
import com.shohiebsense.pokerekom.presentation.settings.SettingsScreen

@Composable
fun PokeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Recommendation.route,
        modifier = modifier
    ) {
        composable(route = Screen.Recommendation.route) {
            RecommendationScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
