# UX Polish Issues

Issues discovered during Phase 3 testing (2025-10-29). These are non-critical UX improvements to address after Phase 4 (History screen) is complete.

---

## Settings Screen Issues

### 1. Snackbar Shows Multiple Times
**Severity:** Low
**Status:** 🔴 Not Fixed

**Description:**
When user saves height in Settings, the "Height saved successfully!" snackbar appears correctly. However, when navigating away from Settings and then back, the snackbar appears again (sometimes multiple times).

**Steps to Reproduce:**
1. Go to Settings screen
2. Enter height (e.g., 175)
3. Tap SAVE → Snackbar appears ✅
4. Navigate to Home screen
5. Navigate back to Settings screen
6. Snackbar appears again 🐛

**Expected Behavior:**
Snackbar should only show once immediately after save action.

**Possible Cause:**
- Snackbar state not being cleared properly
- LaunchedEffect triggering on recomposition

**Files Involved:**
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsViewModel.kt`

---

### 2. Snackbar Blocked by Keyboard
**Severity:** Medium
**Status:** 🔴 Not Fixed

**Description:**
The "Height saved successfully!" snackbar appears at the bottom of the screen. When the keyboard is open (user just finished typing height), the snackbar is hidden behind the keyboard. User must dismiss keyboard to see confirmation.

**Current Behavior:**
```
[Keyboard covering bottom]
[Snackbar hidden behind keyboard] ← User can't see this
```

**Expected Behavior:**
```
[Snackbar at top] ← Visible above keyboard
[Height input field]
[Keyboard at bottom]
```

**Proposed Solution:**
- Move snackbar to top of screen using `SnackbarHostState` with custom positioning
- OR use a top banner/alert instead of snackbar
- OR auto-dismiss keyboard when SAVE is tapped

**Files Involved:**
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsScreen.kt` (line ~100-150, snackbar positioning)

---

### 3. Height Input Field Shows Saved Value (Confusing UX)
**Severity:** Medium
**Status:** 🔴 Not Fixed

**Description:**
When user clicks on the height input field, the previously saved value is still displayed in the field. This creates confusion:
- Is this a draft value or the saved value?
- Should user clear it manually?
- What happens if they navigate away?

**Current Behavior:**
1. User saves height as 175
2. "Current: 175 cm" shows saved value ✅
3. User clicks input field → field shows "175" 🐛
4. User expects field to be empty (ready for new input)

**Expected Behavior:**
Option A: Field starts empty, placeholder shows saved value
```
Input field: [empty, placeholder: "175"]
Below: "Current: 175 cm"
```

Option B: Field clears when clicked
```
User clicks → field clears → ready for typing
```

**Note:** This was partially addressed in progress log (2025-10-25):
> "Separated heightInput (draft) from savedHeight (DB value)"

But the UX still feels confusing to the user.

**Files Involved:**
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsViewModel.kt`

---

## Permission Issues

### 4. Activity Recognition Toggle Doesn't Sync with Actual Permission
**Severity:** High
**Status:** 🔴 Not Fixed

**Description:**
When user grants ACTIVITY_RECOGNITION permission via system dialog (tapping "Allow"), the toggle switch in Settings screen does not update to reflect the granted permission. User has to manually toggle it on.

**Steps to Reproduce:**
1. Open app for first time
2. Tap START on Home screen
3. System permission dialog appears
4. Tap "Allow" → Permission granted ✅
5. Go to Settings screen
6. Activity Recognition toggle is OFF 🐛 (should be ON)
7. User must manually toggle it ON

**Expected Behavior:**
Toggle should automatically reflect the actual system permission state.

**Root Cause:**
The toggle in Settings is storing its own state in DataStore (`activityRecognitionEnabled`), but this is separate from the actual Android system permission. They are not synced.

**Proposed Solution:**
- Remove the toggle's DataStore persistence
- Make toggle read-only or informational only
- Check actual system permission using `ContextCompat.checkSelfPermission()` and display status
- OR: Remove the toggle entirely (permission is already requested at START)

**Files Involved:**
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/stepeeeasy/data/local/datastore/SettingsDataStore.kt`
- `app/src/main/java/com/example/stepeeeasy/domain/repository/SettingsRepository.kt`

---

### 5. Activity Recognition Toggle Doesn't Prevent Step Counting
**Severity:** High
**Status:** 🔴 Not Fixed

**Description:**
Disabling the "Activity Recognition" toggle in Settings does not stop the app from counting steps. The app continues to work normally even when the toggle is OFF.

**Steps to Reproduce:**
1. Grant permission, start walk → steps counting ✅
2. Stop walk
3. Go to Settings
4. Turn OFF "Activity Recognition" toggle
5. Go back to Home
6. Tap START → Walk starts, steps still counting 🐛

**Expected Behavior:**
Option A: Toggle controls whether step counting is enabled (disable toggle = disable feature)
Option B: Toggle is read-only and just shows permission status (doesn't control feature)

**Root Cause:**
The toggle state is stored in DataStore but never actually used/checked by `StepCounterManager` or `HomeViewModel`. The permission check in `HomeScreen.kt` uses the actual system permission, not the toggle state.

**Proposed Solution:**
Either:
1. **Remove the toggle entirely** - Permission is managed via system dialog, no need for app-level toggle
2. **Make it informational only** - Shows permission status, can't be toggled (opens system settings if user wants to revoke)
3. **Implement toggle logic** - Check toggle state in HomeViewModel before starting sensor

**Recommendation:** Remove the toggle. It adds complexity without clear benefit. The wireframes show it, but it's not essential for MVP.

**Files Involved:**
- `app/src/main/java/com/example/stepeeeasy/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/stepeeeasy/presentation/home/HomeViewModel.kt`
- `app/src/main/java/com/example/stepeeeasy/domain/repository/SettingsRepository.kt`

---

## Priority for Fixes

### High Priority (Fix Before Final Submission)
- #4: Activity Recognition toggle doesn't sync
- #5: Activity Recognition toggle doesn't work
- **Recommendation:** Remove the toggle entirely to simplify

### Medium Priority (Important UX)
- #2: Snackbar blocked by keyboard
- #3: Height input field confusing

### Low Priority (Nice to Have)
- #1: Snackbar shows multiple times

---

## When to Fix

**Recommended Timeline:**
1. ✅ **Phase 3 Complete** - Sensor integration works (2025-10-29)
2. ⏳ **Phase 4 Next** - Implement History screen (simple list view)
3. 🔧 **Polish Phase** - Fix all UX issues in this document
4. 🧪 **Final Testing** - End-to-end walkthrough, discover any additional issues

---

## Testing Checklist (After Fixes)

### Settings Screen
- [ ] Snackbar appears only once after save
- [ ] Snackbar visible above keyboard
- [ ] Height input field UX is clear and intuitive
- [ ] Activity Recognition section works as expected (or removed)

### Permissions
- [ ] Permission flow is smooth and clear
- [ ] No confusing toggles or duplicate permission checks
- [ ] User understands what permissions are needed and why

---

**Last Updated:** 2025-10-29
**Phase:** Post-Phase 3 Testing
**Next Milestone:** Phase 4 - History Screen
