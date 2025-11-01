# Phase 4 History Screen - Improvements

**Created:** 2025-11-01
**Status:** Ready to implement
**Priority:** Medium (UX Enhancement)

---

## Overview

Phase 4 History Screen is functional but needs two UX improvements for better data presentation.

---

## Issue 1: Add Time to Walk List Items

### Current Behavior
Walk list items only show day of week and date:
```
Saturday
Nov 01, 2025
```

### Desired Behavior
Show time along with day and date:
```
Saturday
Nov 01, 2025, 16:50
```

### Implementation Location
**File:** `app/src/main/java/com/example/stepeeeasy/presentation/history/WalkListItem.kt`

**Current code (line ~321-325):**
```kotlin
Text(
    text = walk.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

**Proposed fix:**
```kotlin
Text(
    text = walk.startTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, HH:mm")),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

**Note:** Use `walk.startTime` (Instant) instead of `walk.date` (LocalDate) to access time information.

---

## Issue 2: Improve Distance Formatting for Short Walks

### Current Behavior
All walks show `0.0 km` because they are short walks (< 1000 meters).

**Example:**
- 57 steps → 24.5 m → displays as `0.0 km` ❌
- 17 steps → 7.3 m → displays as `0.0 km` ❌

### Desired Behavior

**Case 1: Distance < 1000 meters**
- Show in meters with no decimal places
- Example: `550 m`, `24 m`, `999 m`

**Case 2: Distance >= 1000 meters**
- Show km + remaining meters
- Example:
  - 1055 m → `1 km 55 m`
  - 1500 m → `1 km 500 m`
  - 2000 m → `2 km`
  - 2050 m → `2 km 50 m`

**Case 3: Very long distances (>= 10 km)**
- Show only km with 1 decimal place
- Example: `12.5 km`, `20.3 km`

### Implementation Location

**File:** `app/src/main/java/com/example/stepeeeasy/util/FormatUtils.kt`

**Current code:**
```kotlin
fun formatDistance(distanceKm: Double): String {
    return "%.1f km".format(distanceKm)
}
```

**Proposed fix:**
```kotlin
/**
 * Format distance for display with intelligent unit selection.
 *
 * Rules:
 * - < 1000m: Show in meters (e.g., "24 m", "550 m")
 * - 1000m - 9999m: Show km + meters (e.g., "1 km 55 m", "2 km")
 * - >= 10km: Show km with 1 decimal (e.g., "12.5 km")
 *
 * @param distanceKm Distance in kilometers
 * @return Formatted distance string
 */
fun formatDistance(distanceKm: Double): String {
    val distanceMeters = (distanceKm * 1000).toInt()

    return when {
        // Less than 1 km: show in meters
        distanceMeters < 1000 -> {
            "$distanceMeters m"
        }

        // 1 km to 9.999 km: show km + meters
        distanceMeters < 10000 -> {
            val km = distanceMeters / 1000
            val meters = distanceMeters % 1000

            if (meters == 0) {
                "$km km"
            } else {
                "$km km $meters m"
            }
        }

        // 10 km or more: show km with 1 decimal
        else -> {
            "%.1f km".format(distanceKm)
        }
    }
}
```

### Testing Checklist

After implementing changes, verify these test cases:

**Distance formatting:**
- [ ] 24 meters → `24 m`
- [ ] 550 meters → `550 m`
- [ ] 999 meters → `999 m`
- [ ] 1000 meters → `1 km`
- [ ] 1055 meters → `1 km 55 m`
- [ ] 1500 meters → `1 km 500 m`
- [ ] 2000 meters → `2 km`
- [ ] 9999 meters → `9 km 999 m`
- [ ] 10000 meters → `10.0 km`
- [ ] 12500 meters → `12.5 km`

**Time display:**
- [ ] Walk started at 16:50 shows `Nov 01, 2025, 16:50`
- [ ] Walk started at 09:05 shows `Nov 01, 2025, 09:05`
- [ ] Time uses 24-hour format (HH:mm)

---

## Files to Modify

1. **FormatUtils.kt** - Update `formatDistance()` function
2. **WalkListItem.kt** - Change date formatting to include time

---

## Estimated Time

- 10-15 minutes

---

## Notes

- These improvements will also benefit the Home screen (shows distance during active walk)
- The `formatDistanceFromMeters()` function can remain as-is (used elsewhere)
- Consider adding unit tests for the new `formatDistance()` logic

---

**Status:** Documented, ready for implementation next session
