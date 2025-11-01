# Post-Phase 4 Testing - Quick Fixes

**Created:** 2025-11-01
**Status:** Fixing now
**Testing Session:** After Phase 4 + UX Polish implementation

---

## Issue 1: Height Saved Snackbar Not Showing

**Severity:** Medium

**Description:**
The "Height updated successfully" snackbar is completely gone. It doesn't appear at all after saving height.

**Expected:**
Snackbar should appear once after successfully saving height.

**Fix:**
Revert the snackbar fix - the order was wrong. Should show snackbar first, then reset flag.

---

## Issue 2: Remove Battery Optimization Text

**Severity:** Low

**Description:**
The text "For accurate tracking, disable battery optimization" is shown in Settings footer, but it's not needed since we removed the foreground service.

**Current:**
```
For accurate tracking, disable battery optimization
zerodawn57027@gmail.com
App data stored locally only.
Version 1.0 (Offline)
```

**Expected:**
```
zerodawn57027@gmail.com
App data stored locally only.
Version 1.0 (Offline)
```

**Fix:**
Remove the battery optimization text from AppInfoFooter component.

---

## Issue 3: Add Snackbar for Clear Walks

**Severity:** Medium

**Description:**
When user taps "CLEAR RECORDED WALKS" and confirms, the walks are deleted but there's no feedback to confirm the action was successful.

**Expected:**
Show snackbar: "All walks deleted successfully" after confirmation.

**Fix:**
Add snackbar state to SettingsUiState and show it after clearAllWalksUseCase completes.

---

**Status:** Ready to fix
