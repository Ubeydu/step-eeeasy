package com.example.stepeeeasy.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Home Screen - Main screen for starting/stopping walks.
 *
 * This is where users will:
 * - Tap START to begin a walk
 * - See live timer counting up
 * - See step count and distance (Phase 3 with sensors)
 * - Tap STOP to end the walk
 *
 * @param onNavigateToHistory Callback to navigate to History screen
 * @param viewModel HomeViewModel (automatically injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Collect UI state from ViewModel
    // collectAsStateWithLifecycle is lifecycle-aware - it stops collecting when app is backgrounded
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Active Walk",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { paddingValues ->
        // Content based on current state
        when (val state = uiState) {
            is HomeUiState.Idle -> {
                IdleContent(
                    onStartClicked = viewModel::onStartWalkClicked,
                    onViewHistoryClicked = onNavigateToHistory,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is HomeUiState.WalkActive -> {
                ActiveWalkContent(
                    state = state,
                    onStopClicked = viewModel::onStopWalkClicked,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is HomeUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Content shown when no walk is active.
 *
 * Shows:
 * - Current date
 * - START button
 * - Link to view previous walks
 */
@Composable
private fun IdleContent(
    onStartClicked: () -> Unit,
    onViewHistoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Current date
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // START button
        Button(
            onClick = onStartClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "START",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // View history link
        TextButton(onClick = onViewHistoryClicked) {
            Text(
                text = "View daily stats →",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Content shown when a walk is active.
 *
 * Shows:
 * - Timer (HH:MM:SS)
 * - Step count (placeholder: 0 until Phase 3)
 * - Distance (placeholder: 0.0 km until Phase 3)
 * - STOP button
 */
@Composable
private fun ActiveWalkContent(
    state: HomeUiState.WalkActive,
    onStopClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section - Timer and stats
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Timer display (large)
            Text(
                text = state.formattedTime,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Steps
                StatCard(
                    label = "Steps",
                    value = state.currentSteps.toString()
                )

                // Distance
                StatCard(
                    label = "Distance",
                    value = state.formattedDistance
                )
            }
        }

        // Bottom section - STOP button
        Button(
            onClick = onStopClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "STOP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Stat card component (reusable).
 *
 * Shows a label and a value in a card.
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error content (shown when something goes wrong).
 */
@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
