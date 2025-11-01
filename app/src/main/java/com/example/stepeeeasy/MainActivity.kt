package com.example.stepeeeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.stepeeeasy.presentation.home.HomeScreen
import com.example.stepeeeasy.presentation.history.HistoryScreen
import com.example.stepeeeasy.presentation.settings.SettingsScreen
import com.example.stepeeeasy.ui.theme.StepEeeasyTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point for the app.
 *
 * @AndroidEntryPoint tells Hilt this Activity can receive injected dependencies.
 * This enables Hilt to work with ViewModels in Composables.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StepEeeasyTheme {
                StepEeeasyApp()
            }
        }
    }
}

/**
 * Main app composable with bottom navigation.
 *
 * Manages navigation between Home, History, and Settings screens.
 */
@PreviewScreenSizes
@Composable
fun StepEeeasyApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // Show main navigation with bottom bar
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        // Show different screen based on selected destination
        when (currentDestination) {
            AppDestinations.HOME -> {
                HomeScreen(
                    onNavigateToHistory = { currentDestination = AppDestinations.HISTORY }
                )
            }
            AppDestinations.HISTORY -> {
                HistoryScreen()
            }
            AppDestinations.SETTINGS -> {
                SettingsScreen(
                    onNavigateBack = { currentDestination = AppDestinations.HOME }
                )
            }
        }
    }
}

/**
 * Navigation destinations for bottom navigation bar.
 */
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.Default.DateRange),
    SETTINGS("Settings", Icons.Default.Settings),
}

/**
 * Placeholder screen for unimplemented features.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}