# Snackbar Debug Session

**Issue:** Snackbars show again when navigating away and back to Settings screen

**Affects:**
- Height saved snackbar
- Clear walks snackbar

---

## Problem Analysis

### Current Implementation
```kotlin
LaunchedEffect(uiState.showSuccessSnackbar) {
    if (uiState.showSuccessSnackbar) {
        snackbarHostState.showSnackbar(...)
        viewModel.onSuccessMessageShown()
    }
}
```

### Why This Fails

**Key insight:** `LaunchedEffect(key)` re-runs whenever:
1. The composable is recomposed AND
2. The key value has changed

**Problem scenarios:**
1. User saves height → `showSuccessSnackbar = true`
2. LaunchedEffect runs → shows snackbar
3. ViewModel sets `showSuccessSnackbar = false`
4. User navigates away
5. User navigates back → Settings screen recomposes
6. If the Flow in ViewModel emits the state again, the boolean might become true again
7. LaunchedEffect sees the change and runs again

### Root Cause

The boolean flag approach doesn't work well because:
- Booleans can only toggle between true/false
- Navigation can trigger state re-collection
- The Flow might re-emit values during lifecycle changes

---

## Debugging Steps

1. Add logging to see when snackbar flag changes
2. Check if ViewModel is recreated on navigation
3. Verify when LaunchedEffect actually runs

---

## Proposed Solution

Instead of boolean, use a **counter or timestamp** that increments each time the action happens.

### Before (Boolean - Buggy)
```kotlin
data class SettingsUiState(
    val showSuccessSnackbar: Boolean = false
)

// In ViewModel
_uiState.update { it.copy(showSuccessSnackbar = true) }
_uiState.update { it.copy(showSuccessSnackbar = false) }
```

### After (Counter - Fixed)
```kotlin
data class SettingsUiState(
    val heightSavedEvent: Int = 0  // Increments each time height is saved
)

// In ViewModel
_uiState.update { it.copy(heightSavedEvent = it.heightSavedEvent + 1) }

// In UI
LaunchedEffect(uiState.heightSavedEvent) {
    if (uiState.heightSavedEvent > 0) {
        snackbarHostState.showSnackbar(...)
    }
}
```

**Why this works:**
- Counter always increases, never goes back
- LaunchedEffect only triggers when counter actually changes
- Navigation doesn't cause re-trigger because counter stays the same

---

## Alternative: One-Time Event Pattern

Use a nullable event that gets consumed:

```kotlin
data class SettingsUiState(
    val snackbarMessage: String? = null
)

// Show and consume
LaunchedEffect(uiState.snackbarMessage) {
    uiState.snackbarMessage?.let { message ->
        snackbarHostState.showSnackbar(message)
        viewModel.onSnackbarShown()
    }
}
```

But this has the same problem as boolean.

---

## Recommended Fix: Event Counter Pattern

**Simplest and most reliable for high school project.**
