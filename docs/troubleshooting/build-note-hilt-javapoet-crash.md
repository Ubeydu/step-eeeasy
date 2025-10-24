
---

# Build note — Hilt + JavaPoet crash

## What happened

Build failed on task `:app:hiltAggregateDepsDebug` with:

```
java.lang.NoSuchMethodError:
'java.lang.String com.squareup.javapoet.ClassName.canonicalName()'
```

## Why it happened

* The **Android Gradle Plugin (AGP) 8.13.0** brings **`com.squareup:javapoet:1.10.0`** onto the **plugin classpath**.
* The **Hilt Gradle task** requires **JavaPoet ≥ 1.13.0** (that’s where `ClassName.canonicalName()` exists).
* Result: Hilt task loads the **older** JavaPoet from the plugin classpath → `NoSuchMethodError`.

How we confirmed:

```bash
./gradlew buildEnvironment --info | egrep -n "classpath|com\.squareup:javapoet"
# showed: com.squareup:javapoet:1.10.0
```

## How we resolved it (current stable fix)

1. Align Hilt versions (plugin + libs) in `gradle/libs.versions.toml`:

```toml
[versions]
hilt = "2.51.1"
```

2. **Pin JavaPoet on the plugin classpath** in root `build.gradle.kts`:

```kotlin
buildscript {
  configurations.classpath {
    // AGP 8.13.0 drags javapoet:1.10.0 which lacks ClassName.canonicalName().
    // Hilt’s AggregateDepsTask needs ≥ 1.13.0.
    resolutionStrategy.force("com.squareup:javapoet:1.13.0")
  }
}
```

3. Verify:

```bash
./gradlew buildEnvironment --info | egrep -n "classpath|com\.squareup:javapoet"
# expect: com.squareup:javapoet:1.10.0 -> 1.13.0
./gradlew app:assembleDebug
# expect: BUILD SUCCESSFUL
```

## What to do later (long-term fix)

When you decide to update Gradle/Android bits:

1. **Upgrade AGP** to a stable version where `javapoet:1.10.0` no longer appears on the plugin classpath.

    * After bumping AGP, run:

   ```bash
   ./gradlew buildEnvironment --info | egrep -n "classpath|com\.squareup:javapoet"
   ```

    * If you no longer see `1.10.0`, remove the `force("…:1.13.0")` block.

2. Keep Hilt **plugin and libs** on the **same version** in `libs.versions.toml`.

3. (Optional) Use the Versions plugin to see safe updates without changing anything:

```kotlin
// root build.gradle.kts
plugins {
  id("com.github.ben-manes.versions") version "0.53.0"
}
```

```bash
./gradlew dependencyUpdates
```

## If something breaks again

* **Symptom**: same `NoSuchMethodError` for `ClassName.canonicalName()`.
* **Immediate check**:

  ```bash
  ./gradlew buildEnvironment --info | egrep -n "classpath|com\.squareup:javapoet"
  ```

    * If `1.10.0` is present → keep (or re-add) the `force("…:1.13.0")`.
    * If only `≥ 1.13.0` appears → the issue is elsewhere (collect the `Caused by:` block and investigate).

---