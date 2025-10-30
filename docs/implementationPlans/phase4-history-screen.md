# Phase 4: History Screen Implementation Plan

**Created:** 2025-10-29
**Status:** Ready to Implement
**Estimated Time:** 30-40 minutes

---

## Overview

Implement a simple list view to display all completed walks. This is the final core feature before UX polish.

**Goal:** Users can view a scrollable list of all their past walks with ID, distance, steps, and date.

---

## Prerequisites (Already Complete ✅)

- Database layer: WalkDao has `getAllWalks()` query
- Repository layer: WalkRepository has `getAllWalks(): Flow<List<Walk>>`
- Domain model: Walk class with all necessary fields
- FormatUtils: Distance and steps formatting ready
- Navigation: 3-tab bottom navigation structure exists

---

## Files to Create

### 1. GetAllWalksUseCase.kt

**Path:** `app/src/main/java/com/example/stepeeeasy/domain/usecase/GetAllWalksUseCase.kt`

**Purpose:** Fetch all completed walks from repository, filter out active walks

```kotlin
package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for fetching all completed walks.
 *
 * Filters out active walks (is_active = true) to only show completed walks.
 * Walks are already sorted by start_time DESC in the DAO.
 */
class GetAllWalksUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    /**
     * Get all completed walks as a Flow.
     *
     * @return Flow emitting list of completed walks, sorted newest first
     */
    operator fun invoke(): Flow<List<Walk>> {
        return walkRepository.getAllWalks()
            .map { walks ->
                walks.filter { !it.isActive }  // Exclude active walk
            }
    }
}
```

---

### 2. HistoryViewModel.kt

**Path:** `app/src/main/java/com/example/stepeeeasy/presentation/history/HistoryViewModel.kt`

**Purpose:** Manage History screen state and load walks

```kotlin
package com.example.stepeeeasy.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.usecase.GetAllWalksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for History screen.
 *
 * Responsibilities:
 * - Load all completed walks using GetAllWalksUseCase
 * - Expose walks as StateFlow for UI to collect
 * - Handle empty state
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    getAllWalksUseCase: GetAllWalksUseCase
) : ViewModel() {

    /**
     * StateFlow of all completed walks.
     * Automatically updates when walks are added/removed.
     * Empty list when no walks exist.
     */
    val walks: StateFlow<List<Walk>> = getAllWalksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

---

### 3. HistoryScreen.kt

**Path:** `app/src/main/java/com/example/stepeeeasy/presentation/history/HistoryScreen.kt`

**Purpose:** Display scrollable list of all completed walks

```kotlin
package com.example.stepeeeasy.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * History Screen - Displays list of all completed walks.
 *
 * Shows:
 * - Top bar with "Walks History" title
 * - Scrollable list of walks (newest first)
 * - Empty state when no walks exist
 *
 * @param viewModel HistoryViewModel (automatically injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    // Collect walks from ViewModel
    val walks by viewModel.walks.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Walks History",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { paddingValues ->
        if (walks.isEmpty()) {
            // Empty state - no walks yet
            EmptyStateMessage(
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // List of walks
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = walks,
                    key = { walk -> walk.id }
                ) { walk ->
                    WalkListItem(walk = walk)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state message when no walks exist.
 */
@Composable
private fun EmptyStateMessage(
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
            text = "No walks yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start your first walk from the Home screen!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

---

### 4. WalkListItem.kt

**Path:** `app/src/main/java/com/example/stepeeeasy/presentation/history/WalkListItem.kt`

**Purpose:** Individual walk entry in the list

```kotlin
package com.example.stepeeeasy.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.util.FormatUtils
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Walk list item composable.
 *
 * Layout:
 * ┌────────────────────────────────────────────┐
 * │  #1    │  5.8 km        │  Sunday         │
 * │        │  7,860 steps   │  Oct 29, 2025   │
 * └────────────────────────────────────────────┘
 *
 * Three columns:
 * - Left: Walk ID
 * - Middle: Distance + Steps
 * - Right: Day of week + Date
 */
@Composable
fun WalkListItem(
    walk: Walk,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Walk ID
            Text(
                text = "#${walk.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Middle: Distance + Steps (stacked)
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            ) {
                Text(
                    text = FormatUtils.formatDistance(walk.distanceKm),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${FormatUtils.formatSteps(walk.totalSteps)} steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Day of week + Date (stacked)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = walk.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = walk.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

---

## Files to Modify

### 5. MainActivity.kt

**Path:** `app/src/main/java/com/example/stepeeeasy/MainActivity.kt`

**Change:** Replace PlaceholderScreen with HistoryScreen for HISTORY destination

**Find this code:**
```kotlin
AppDestinations.HISTORY -> {
    PlaceholderScreen(title = "History", message = "Coming in Phase 4!")
}
```

**Replace with:**
```kotlin
AppDestinations.HISTORY -> {
    HistoryScreen()
}
```

**Also add import:**
```kotlin
import com.example.stepeeeasy.presentation.history.HistoryScreen
```

---

## Implementation Steps

### Step 1: Create GetAllWalksUseCase
1. Create file at `domain/usecase/GetAllWalksUseCase.kt`
2. Copy code from above
3. Verify imports resolve

### Step 2: Create HistoryViewModel
1. Create file at `presentation/history/HistoryViewModel.kt`
2. Copy code from above
3. Verify Hilt injection works

### Step 3: Create WalkListItem Composable
1. Create file at `presentation/history/WalkListItem.kt`
2. Copy code from above
3. Test formatting functions

### Step 4: Create HistoryScreen Composable
1. Create file at `presentation/history/HistoryScreen.kt`
2. Copy code from above
3. Verify LazyColumn and empty state

### Step 5: Update MainActivity Navigation
1. Open `MainActivity.kt`
2. Find HISTORY destination
3. Replace PlaceholderScreen with HistoryScreen
4. Add import statement

### Step 6: Build and Test
1. Run `./gradlew compileDebugKotlin`
2. Fix any compilation errors
3. Run `./gradlew assembleDebug`
4. Install on device: `./gradlew installDebug`

---

## Testing Checklist

### Manual Testing on Device

**Setup:**
1. Open app on real Android device
2. Complete 3-5 walks with different step counts
3. Navigate to History screen via bottom navigation

**Verify:**
- [ ] All completed walks appear in list
- [ ] Walks are ordered newest first (most recent at top)
- [ ] Walk IDs increment correctly (#1, #2, #3...)
- [ ] Distance displays with 1 decimal place (e.g., "7.5 km")
- [ ] Steps display with comma separators (e.g., "10,000 steps")
- [ ] Day of week displays correctly (e.g., "Sunday")
- [ ] Date formats correctly (e.g., "Oct 29, 2025")
- [ ] List is scrollable if many walks
- [ ] Empty state shows when no walks exist
- [ ] Active walk (if any) is NOT shown in history

### Edge Cases to Test

- [ ] No walks yet (empty state)
- [ ] Single walk
- [ ] Many walks (10+) - verify scrolling
- [ ] Walk with 0 steps (edge case)
- [ ] Walk with very high steps (e.g., 50,000)
- [ ] Walks on same day (multiple entries)

---

## Common Issues and Solutions

### Issue: "Unresolved reference: HistoryScreen"
**Solution:** Make sure to add import in MainActivity:
```kotlin
import com.example.stepeeeasy.presentation.history.HistoryScreen
```

### Issue: Empty list shows even though walks exist
**Solution:** Check that `isActive = false` filter is working in GetAllWalksUseCase

### Issue: Walks not in correct order
**Solution:** Verify WalkDao query has `ORDER BY start_time DESC`

### Issue: Date formatting shows wrong locale
**Solution:** Use `Locale.getDefault()` in dayOfWeek.getDisplayName()

---

## Code References

### FormatUtils Methods Used
- `FormatUtils.formatDistance(distanceKm: Double): String` → "7.5 km"
- `FormatUtils.formatSteps(steps: Int): String` → "10,000"

### Walk Domain Model Properties
- `walk.id: Long` → Walk ID from database
- `walk.distanceKm: Double` → Distance in kilometers (computed property)
- `walk.totalSteps: Int` → Total step count
- `walk.date: LocalDate` → Date of walk

### Date Formatting
- Day of week: `walk.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())`
- Date: `walk.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))`

---

## Next Steps After Phase 4

Once History screen is complete:

1. **UX Polish** - Fix issues documented in `docs/issues/ux-polish-issues.md`
2. **Final Testing** - End-to-end walkthrough of entire app
3. **README Update** - Add screenshots and usage instructions
4. **Presentation Prep** - Prepare demo for high school project submission

---

## Notes

- **No charts needed** - This is simplified version (Phase 4 from simplified plan)
- **No weekly aggregation** - Just a flat list of all walks
- **Keep it simple** - This is a high school project, prioritize working code over complexity
- **Reactive updates** - Flow from repository ensures UI updates automatically when walks are added

---

**Status:** Ready to implement
**Last Updated:** 2025-10-29
**Next Session:** Start with Step 1 (GetAllWalksUseCase)
