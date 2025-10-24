package com.example.stepeeeasy.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Settings Screen - Phase 2 implementation.
 *
 * Features:
 * - Height input with validation
 * - Activity Recognition toggle
 * - Clear walks button with confirmation
 * - App info footer
 *
 * @param modifier Modifier for styling
 * @param onNavigateBack Callback to navigate back to Home
 * @param viewModel SettingsViewModel (injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success snackbar when save is successful
    LaunchedEffect(uiState.showSuccessSnackbar) {
        if (uiState.showSuccessSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Height saved successfully",
                duration = SnackbarDuration.Short
            )
            viewModel.onSuccessMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main content
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Height input section
                HeightInputSection(
                    heightInput = uiState.heightInput,
                    errorMessage = uiState.errorMessage,
                    onHeightChanged = viewModel::onHeightChanged,
                    onSaveClicked = viewModel::onSaveHeight
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Activity Recognition toggle
                ActivityRecognitionSection(
                    enabled = uiState.activityRecognitionEnabled,
                    onToggled = viewModel::onActivityRecognitionToggled
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Clear walks button
                Button(
                    onClick = viewModel::onClearWalksClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("CLEAR RECORDED WALKS")
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(32.dp))
            AppInfoFooter()
        }
    }

    // Confirmation dialog
    if (uiState.showClearDialog) {
        ClearWalksConfirmationDialog(
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onConfirmClearWalks
        )
    }
}

/**
 * Height input section with TextField and SAVE button.
 */
@Composable
private fun HeightInputSection(
    heightInput: String,
    errorMessage: String?,
    onHeightChanged: (String) -> Unit,
    onSaveClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Height (cm)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = heightInput,
                onValueChange = onHeightChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorMessage != null
            )

            Button(
                onClick = onSaveClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("SAVE")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Error message or helper text
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = "Current: $heightInput cm • Used to estimate stride length automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Activity Recognition toggle section.
 */
@Composable
private fun ActivityRecognitionSection(
    enabled: Boolean,
    onToggled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Activity Recognition",
            style = MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = enabled,
            onCheckedChange = onToggled
        )
    }
}

/**
 * App info footer.
 */
@Composable
private fun AppInfoFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "For accurate tracking, disable battery optimization",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "zerodawn57027@gmail.com",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "App data stored locally only.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Version 1.0 (Offline)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Confirmation dialog for clearing walks.
 */
@Composable
private fun ClearWalksConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Clear All Walks?")
        },
        text = {
            Text("This will permanently delete all recorded walks and GPS data. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("CONFIRM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
