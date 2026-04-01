package com.shohiebsense.pokerekom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.shohiebsense.pokerekom.presentation.navigation.PokeNavHost
import com.shohiebsense.pokerekom.ui.theme.PokerekomTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokerekomTheme {
                val navController = rememberNavController()
                PokeNavHost(navController = navController)
            }
        }
    }
}
