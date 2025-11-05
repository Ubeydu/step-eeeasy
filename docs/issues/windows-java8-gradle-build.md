# Windows Build Issue: Java 8 vs Java 11+ Requirement

**Created:** 2025-11-01
**Status:** Known Issue - Workaround Available
**Platform:** Windows
**Severity:** Medium (affects command line builds only)

---

## Issue Summary

When running `./gradlew compileDebugKotlin` on Windows, build fails with:

```
Dependency requires at least JVM runtime version 11. This build uses a Java 8 JVM.
```

---

## Root Cause

- **System Java:** Windows machine has Java 8 installed
- **Project Requirements:** Android Gradle Plugin 8.13.0 requires Java 11+
- **Why it happens:** Running `./gradlew` from command line uses system Java (not Android Studio's embedded JDK)

---

## ✅ Solution 1: Build from Android Studio (RECOMMENDED)

Android Studio uses its own embedded JDK (Java 17) automatically.

**Steps:**
1. Open project in Android Studio
2. Click elephant icon 🐘 (Sync Project with Gradle Files)
3. Build → Make Project (or Ctrl+F9)

**Result:** ✅ Works perfectly

---

## Solution 2: Set JAVA_HOME Temporarily

Point command line to Android Studio's embedded JDK.

**PowerShell:**
```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew compileDebugKotlin
```

**Command Prompt:**
```cmd
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
./gradlew compileDebugKotlin
```

---

## Solution 3: Local gradle.properties Override

Add to `gradle.properties` (DON'T commit):

```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

Then mark file as skip-worktree:
```bash
git update-index --skip-worktree gradle.properties
```

---

## Context: Cross-Platform Fix

This issue was discovered while testing the fix for the macOS hardcoded Java home path issue.

**Before fix:** `gradle.properties` had hardcoded macOS path → blocked Windows builds
**After fix:** Removed hardcoded path → revealed Windows system Java 8 issue

The cross-platform fix (removing hardcoded path) is correct. The Java 8 issue is unrelated and exists on the Windows machine's environment.

---

## Recommendation

**For high school project:** Use Android Studio to build (Solution 1). Command line builds are optional.

**For team collaboration:** Each developer should use Android Studio's built-in build tools rather than terminal commands.

---

**Related:**
- Branch: `fix/gradle-java-home`
- Original issue: Hardcoded macOS Java home path in gradle.properties
