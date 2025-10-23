# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

StepEeeasy (package: `com.example.stepeeeasy`) is an Android walking tracker application built with Jetpack Compose and Material Design 3. The app records walk sessions with step counting, distance tracking, and elevation monitoring. All data is stored locally for privacy and offline functionality.

**Target Platform:** Android API 34+ (minSdk: 34, targetSdk: 36)
**Language:** Kotlin
**UI Framework:** Jetpack Compose with Material 3
**Architecture Goal:** Clean Architecture + Repository Pattern (see docs/android-development-plan-v1.md)

## Build Commands

### Standard Development
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean

# Run lint checks
./gradlew lint
```

### Running Tests
```bash
# Run all tests
./gradlew test connectedAndroidTest

# Run only unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew test --tests com.example.stepeeeasy.ExampleUnitTest
```

### Gradle Tasks
```bash
# List all available tasks
./gradlew tasks

# Check dependencies
./gradlew dependencies

# Generate build reports
./gradlew build --scan
```

## Architecture & Code Structure

### Current State (Early Stage)
The project is in early development with basic scaffolding. The current implementation includes:
- **MainActivity.kt**: Entry point with adaptive navigation scaffold supporting Home, Favorites, and Profile destinations
- **Compose UI Theme**: Material 3 theming with dynamic color support (Android 12+)
- **Navigation**: Basic bottom navigation using NavigationSuiteScaffold for adaptive layouts

### Planned Architecture (Clean Architecture)
The comprehensive architecture plan is documented in `docs/android-development-plan-v1.md`. Key layers:

1. **Presentation Layer** (`presentation/`):
   - Composables for Home, History, Paths, and Settings screens
   - ViewModels with LiveData/Flow for state management
   - Navigation management

2. **Domain Layer** (`domain/`):
   - Business logic and use cases (StartWalkUseCase, StopWalkUseCase, etc.)
   - Domain models (Walk, GpsPoint, DailyStats)
   - Repository interfaces

3. **Data Layer** (`data/`):
   - Room database with WalkEntity and GpsPointEntity
   - Repository implementations
   - Local data storage with DataStore for settings

4. **Service Layer** (`service/`):
   - WalkTrackingService (foreground service for background tracking)
   - SensorManager (step counter integration)
   - GpsLocationManager (location tracking)

5. **Dependency Injection** (`di/`):
   - Hilt modules for dependency injection

### Database Schema
Room database with two main entities:
- **WalkEntity**: Stores walk sessions (start_time, end_time, total_steps, distance_meters, elevation_gain_meters, is_active, date)
- **GpsPointEntity**: Stores GPS coordinates for path visualization (walk_id FK, latitude, longitude, altitude, timestamp, accuracy)

## Development Guidelines

### Compose UI Patterns
- Use `@PreviewScreenSizes` for responsive design testing
- Leverage Material 3 adaptive components (NavigationSuiteScaffold)
- Follow the theme defined in `ui/theme/Theme.kt` with dynamic color support
- State management with `rememberSaveable` for configuration changes

### Architecture Implementation
When adding features, follow this structure:
1. Define domain model in `domain/model/`
2. Create repository interface in `domain/repository/`
3. Implement repository in `data/repository/`
4. Create use case in `domain/usecase/`
5. Build ViewModel in `presentation/[screen]/`
6. Implement Composable UI in `presentation/[screen]/`

### Sensor & Permission Handling
- Step counting requires `ACTIVITY_RECOGNITION` permission (API 29+)
- GPS tracking requires `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`
- Background tracking requires `FOREGROUND_SERVICE` permission
- Always request runtime permissions before accessing sensors
- Use foreground service with notification for continuous tracking

### Data Persistence
- Use Room for structured data (walks, GPS points)
- Use DataStore (Preferences) for settings (user height, permissions)
- Always use Flow for reactive database queries
- Perform database operations on IO dispatcher

### Testing Strategy
- Unit tests for ViewModels, UseCases, and Utilities (e.g., DistanceCalculator)
- Instrumented tests for Repository and DAO implementations
- Test files located in:
  - `app/src/test/` for unit tests
  - `app/src/androidTest/` for instrumented tests

## Key Dependencies

Reference `gradle/libs.versions.toml` for version management:
- **Compose BOM**: 2024.09.00 (Material 3, UI components)
- **Kotlin**: 2.0.21
- **AGP**: 8.13.0
- **AndroidX Core**: 1.17.0
- **Lifecycle**: 2.6.1
- **Activity Compose**: 1.8.0

Planned additions (see docs):
- Room (database)
- Hilt (dependency injection)
- DataStore (settings)
- Play Services (Maps, Location)
- MPAndroidChart (history charts)

## Important Notes

### Project Context
- **High school final project** - prioritize clarity and maintainability
- **Offline-first** - all data stored locally, no cloud dependencies
- **Privacy-focused** - no data collection or external transmission

### Implementation Priority

**Note:** The architecture plan in `docs/android-development-plan-v1.md` references XML layouts and Fragments, but the current codebase uses Jetpack Compose. Adapt the implementation steps to use Composables instead of Fragments/XML.

#### Phase 1: Foundation & Home Screen (Week 1)
1. Set up Hilt dependency injection
2. Create Room database with WalkEntity and GpsPointEntity
3. Implement WalkRepository (data layer)
4. Create StartWalkUseCase and StopWalkUseCase
5. Build HomeViewModel
6. Implement Home screen Composable with basic UI
7. Add timer functionality and walk state management

**Deliverable:** Users can tap "START", see a live timer, and tap "STOP". No sensor integration yet.

#### Phase 2: Settings Screen (Week 1-2)
1. Create SettingsRepository with DataStore
2. Build Settings Composable and SettingsViewModel
3. Add height input field
4. Implement Activity Recognition toggle
5. Add "Clear Recorded Walks" button
6. Display footer information

**Deliverable:** Settings are persistent and affect calculations. User height is now available for stride estimation.

#### Phase 3: Sensor Integration (Week 2)
1. Implement SensorManager for step counting
2. Integrate GpsLocationManager for location tracking
3. Create WalkTrackingService for background tracking
4. Request runtime permissions (Location + Activity Recognition)
5. Update HomeViewModel to display real steps and distance (using actual user height from Settings)
6. Test on actual Android device

**Deliverable:** Live step counter and distance calculation during walks, with stride length based on user's configured height.

#### Phase 4: History Screen (Week 3)
1. Implement getDailyStats usecase
2. Create data aggregation logic (group walks by day)
3. Integrate MPAndroidChart library for bar chart
4. Build History Composable and HistoryViewModel
5. Add week navigation arrows
6. Implement tap-to-show tooltip

**Deliverable:** Users can browse historical data with interactive charts.

#### Phase 5: Paths Screen (Week 4)
1. Fetch GPS points for each walk
2. Integrate Google Maps SDK for rendering path polylines
3. Build Paths Composable with LazyColumn for list rendering
4. Implement PathsViewModel
5. Add lazy loading for map thumbnails

**Deliverable:** Visual representation of all recorded walks with GPS paths.

### Design Decisions
- **Room ORM**: Type-safe, compile-time checked database access
- **Flow/LiveData**: Reactive state management for UI consistency
- **Foreground Service**: Ensures background tracking reliability
- **Built-in Step Counter**: More accurate and battery-efficient than accelerometer-based detection
- **GPS for Paths**: Enables visual map representation over dead reckoning

### Current Package Structure
```
com.example.stepeeeasy/
├── MainActivity.kt
└── ui/theme/
    ├── Color.kt
    ├── Type.kt
    └── Theme.kt
```

As development progresses, expand to:
```
com.example.stepeeeasy/
├── di/              # Hilt modules
├── data/            # Repositories, DAOs, entities
├── domain/          # Models, use cases, interfaces
├── presentation/    # ViewModels and Composables
├── service/         # Background services
├── util/            # Utilities (DistanceCalculator, etc.)
└── MainActivity.kt
```

## Contact & Documentation

- **Contact**: zerodawn57027@gmail.com
- **Architecture Plan**: `docs/android-development-plan-v1.md`
- **Feature Overview**: `docs/app-main-features-v1.md`
- **Complexity Analysis**: `docs/feature-complexity-analysis.md`
