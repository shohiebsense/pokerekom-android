package com.shohiebsense.pokerekom.presentation.navigation

sealed class Screen(val route: String) {
    data object Recommendation : Screen("recommendation")
    data object Settings : Screen("settings")
}
