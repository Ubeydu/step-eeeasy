package com.example.stepeeeasy.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepeeeasy.util.FormatUtils
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val walkStoppedEvent by viewModel.walkStoppedEvent.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when walk is stopped
    LaunchedEffect(walkStoppedEvent) {
        if (walkStoppedEvent > 0) {
            snackbarHostState.showSnackbar(
                message = "Walk saved! View it in History.",
                duration = SnackbarDuration.Short
            )
        }
    }

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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
 *
 * Handles runtime permission request for ACTIVITY_RECOGNITION
 */
@Composable
private fun IdleContent(
    onStartClicked: () -> Unit,
    onViewHistoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start the walk
            onStartClicked()
        } else {
            // Permission denied, show rationale
            showPermissionRationale = true
        }
    }

    // Check if permission is already granted
    fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, start walk
                onStartClicked()
            }
            else -> {
                // Request permission
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }
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

        // START button (with permission check)
        Button(
            onClick = { checkAndRequestPermission() },
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

        // Permission rationale dialog
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                title = { Text("Permission Required") },
                text = {
                    Text("This app needs Activity Recognition permission to count your steps during walks. Please grant the permission to use this feature.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionRationale = false
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    ) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationale = false }) {
                        Text("Cancel")
                    }
                }
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
                    value = FormatUtils.formatSteps(state.currentSteps)
                )

                // Distance
                StatCard(
                    label = "Distance",
                    value = FormatUtils.formatDistanceFromMeters(state.currentDistanceMeters)
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
