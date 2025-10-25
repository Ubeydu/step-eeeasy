# StepEeeasy - Android/Kotlin Development Plan (Simplified)

**Project Type:** High School Final Project
**Target Platform:** Android (API 34+)
**Architecture:** Clean Architecture + Repository Pattern
**UI Framework:** Jetpack Compose + Material Design 3
**Database:** Room ORM with SQLite
**State Management:** ViewModel + StateFlow/LiveData
**Priority:** Home → Settings → History

---

## How to Use This Document

This plan is designed to guide implementation in a structured way:

1. **Read this document first** to understand the overall strategy and implementation order
2. **Refer to `docs/technical-reference-compose.md`** when you need code examples and implementation details
3. **Implementation phases** are ordered by priority and dependency

**Note:** This is a **simplified version** designed to be manageable for a high school final project. We've removed GPS tracking, path visualization, and complex charting to focus on core walking tracker functionality.

---

## 1. Project Overview

### What is StepEeeasy?

StepEeeasy is an **offline-first walking tracker** for Android that records walk sessions with:
- Real-time step counting (using built-in step counter sensor)
- Distance tracking (stride-based calculation from user height)
- Simple historical walk list

### Key Principles

- **Privacy-First:** All data stored locally, no cloud dependencies
- **Offline-First:** Full functionality without internet connection
- **Battery-Efficient:** Uses hardware sensors, not accelerometer polling
- **Simple & Clean:** Easy to understand and maintain
- **Testable:** Clear layer boundaries enable unit testing

### Current Project State

The project has been initialized with:
- Android Studio Compose template
- Basic MainActivity with adaptive navigation scaffold
- Material 3 theme with dynamic color support
- Bottom navigation structure (Home, History, Settings)

**Next Steps:** Follow the implementation roadmap below to build out the full feature set.

---

## 2. Architecture Overview

### Simplified Architecture (No GPS/Paths)

```
┌─────────────────────────────────────────────────────────┐
│  Presentation Layer (UI)                                 │
│  - Composables (Home, History, Settings)                 │
│  - ViewModels (State Management)                         │
│  - Navigation                                            │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│  Domain Layer (Business Logic)                           │
│  - Use Cases                                             │
│  - Domain Models                                         │
│  - Repository Interfaces                                 │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│  Data Layer (Data Access)                                │
│  - Repository Implementations                            │
│  - Room Database (DAOs, Entities)                        │
│  - DataStore (Settings)                                  │
└─────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.example.stepeeeasy/
├── di/                    # Dependency Injection (Hilt)
├── data/                  # Data Layer
│   ├── local/
│   │   ├── database/      # Room entities, DAOs, database
│   │   └── datastore/     # DataStore for settings
│   └── repository/        # Repository implementations
├── domain/                # Domain Layer
│   ├── model/             # Domain models
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic use cases
├── presentation/          # Presentation Layer
│   ├── home/              # Home screen composables + ViewModel
│   ├── settings/          # Settings screen composables + ViewModel
│   ├── history/           # History screen composables + ViewModel
│   ├── navigation/        # Navigation setup (NavHost, routes)
│   └── theme/             # Compose theme (colors, typography)
├── service/               # Background Services
│   ├── WalkTrackingService.kt
│   └── SensorManager.kt
├── util/                  # Utilities
│   └── StrideCalculator.kt
└── MainActivity.kt
```

### Database Schema (Simplified)

**WalkEntity** - Single table for walk sessions:
- `id` (PRIMARY KEY)
- `start_time` (timestamp)
- `end_time` (timestamp, nullable)
- `total_steps` (integer)
- `distance_meters` (double)
- `is_active` (boolean)
- `date` (text, format: "YYYY-MM-DD")

**Note:** No GpsPointEntity - we've removed GPS tracking to simplify the project.

### Data Flow Example

```
User taps START button
    ↓
HomeScreen Composable triggers event
    ↓
HomeViewModel calls StartWalkUseCase
    ↓
StartWalkUseCase calls WalkRepository.startWalk()
    ↓
WalkRepository inserts WalkEntity to Room database
    ↓
Database emits Flow<Walk> update
    ↓
HomeViewModel collects Flow and updates UI state
    ↓
HomeScreen recomposes with new state
```

---

## 3. Implementation Roadmap (Phase-Based)

### Phase 1: Foundation & Home Screen (Week 1)

**Goal:** Basic walk tracking without sensors - timer and database persistence

#### Tasks

1. **Set up Hilt Dependency Injection**
   - Configure application class with @HiltAndroidApp
   - Add Hilt dependencies to build.gradle.kts
   - Create AppModule for core dependencies

2. **Create Room Database Schema**
   - Define WalkEntity (id, start_time, end_time, total_steps, distance_meters, is_active, date)
   - Create WalkDao interface
   - Create AppDatabase class
   - **Note:** No GpsPointEntity needed - we're not tracking paths

3. **Implement WalkRepository**
   - Implement IWalkRepository interface
   - Add startWalk(), stopWalk(), getActiveWalk() methods
   - Handle entity-to-domain model mapping

4. **Create Domain Use Cases**
   - StartWalkUseCase
   - StopWalkUseCase

5. **Build HomeViewModel**
   - Manage walk state (idle, active)
   - Implement timer logic (count elapsed seconds)
   - Expose UI state as StateFlow

6. **Create Home Screen Composable**
   - Display current date at top
   - Show "Active Walk" status indicator (green pill)
   - Display timer (HH:MM:SS format)
   - Show large step count display in circular container
   - Show distance below (km)
   - Display START/STOP buttons at bottom
   - "View previous walks →" link button
   - Bottom navigation bar (Home, History, Settings)

7. **Update Navigation**
   - Configure NavHost with Home destination
   - Set up bottom navigation with 3 tabs: Home, History, Settings
   - **Note:** Settings is in bottom nav, NOT as a gear icon

**Deliverable:** Users can tap "START", see a live timer counting up, and tap "STOP". Walk data is persisted to database. No sensor integration yet.

**Verification:**
```bash
# Start the app, tap START, wait 30 seconds, tap STOP
# Check database with:
adb shell
run-as com.example.stepeeeasy
cd databases
sqlite3 stepeeeasy.db
SELECT * FROM walks;
# Should see one record with correct timestamps
```

---

### Phase 2: Settings Screen (Week 1-2)

**Goal:** Persistent user preferences that affect stride calculations

#### Tasks

1. **Create Settings DataStore**
   - Define preferences keys (user_height, activity_recognition_enabled)
   - Create SettingsDataStore wrapper

2. **Implement SettingsRepository**
   - Save/load user height
   - Save/load activity recognition preference
   - Expose settings as Flow

3. **Create SettingsViewModel**
   - Manage settings state
   - Handle user height updates
   - Handle clear walks action

4. **Build Settings Screen Composable**
   - Top bar with back arrow and "Settings" title
   - Height input field with "SAVE" button
   - Display current height with explanation text
   - Activity Recognition toggle (Switch)
   - "CLEAR RECORDED WALKS" button (green, full width)
   - Footer with:
     - Battery optimization tip
     - Contact email
     - "App data stored locally only"
     - Version info
   - Bottom navigation bar

5. **Add Clear Walks Use Case**
   - ClearAllWalksUseCase
   - Call repository.deleteAllWalks()
   - Show confirmation dialog before deletion

6. **Update Navigation for Settings**
   - Add Settings destination to NavHost
   - Connect bottom navigation Settings tab
   - Settings screen shows standard back arrow to return

**Deliverable:** Settings screen is functional. User can input height, and it persists across app restarts. "Clear Recorded Walks" deletes all walk data.

**Verification:**
```bash
# Enter height as 175, close app, reopen
# Height field should still show 175
# Tap "Clear Recorded Walks", confirm
# Check database - walks table should be empty
```

---

### Phase 3: Sensor Integration (Week 2)

**Goal:** Live step counting during walks with stride-based distance calculation

#### Tasks

1. **Request Runtime Permissions**
   - Add permissions to AndroidManifest.xml:
     - ACTIVITY_RECOGNITION (for step counter)
     - FOREGROUND_SERVICE
     - POST_NOTIFICATIONS
   - Implement permission request flow in Home screen
   - Handle permission denial gracefully

2. **Implement SensorManager**
   - Register step counter sensor listener
   - Track steps since walk started (baseline offset)
   - Expose step count via callback
   - Update every second (throttle sensor events)

3. **Create WalkTrackingService**
   - Foreground service with persistent notification
   - Orchestrate SensorManager
   - Update notification with live step count
   - Handle START/STOP intents
   - Update WalkEntity in database with current steps

4. **Integrate StrideCalculator Utility**
   - Calculate stride length from user height
   - Formula: stride_length_meters = height_cm × 0.43 / 100
   - Calculate distance: steps × stride_length

5. **Update HomeViewModel**
   - Start/stop WalkTrackingService
   - Observe active walk from repository
   - Display real steps from sensor
   - Calculate and display distance using stride calculation
   - Update timer based on walk duration

6. **Update Home Screen UI**
   - Show real-time step count from sensor
   - Show real-time distance (km) calculated from steps
   - Show live timer

**Deliverable:** When user starts a walk, the app counts steps using hardware sensor, calculates distance based on stride length (from user height), and displays all data in real-time. Background tracking continues even when app is minimized.

**Verification:**
```bash
# Grant permissions, tap START
# Walk around with phone (can be indoors)
# Verify step count increases
# Verify distance increases proportionally
# Minimize app - notification should show live step count
# Return to app after 2 minutes, verify timer is accurate
# Tap STOP, check database for final step count and distance
```

---

### Phase 4: History Screen (Week 3)

**Goal:** Simple list view of all completed walks

#### Tasks

1. **Create GetAllWalksUseCase**
   - Fetch all completed walks from repository
   - Sort by date (most recent first)
   - Return as List<Walk>

2. **Add Query Methods to WalkDao**
   - getAllWalks() ordered by start_time DESC
   - Filter out active walks (is_active = false)

3. **Create HistoryViewModel**
   - Load all walks using GetAllWalksUseCase
   - Expose as StateFlow<List<Walk>>
   - Format walk data for display

4. **Build History Screen Composable**
   - Top bar with back arrow and "Walks History" title
   - LazyColumn of walk entries
   - Each walk item shows:
     - Walk ID on left (#1, #2, #3...)
     - Distance and steps in middle
     - Day of week and date on right (e.g., "Sunday Oct 18, 2025")
   - Format distance as "X.X km"
   - Format steps with comma separator (e.g., "7,860 steps")
   - Bottom navigation bar
   - **Note:** No charts, no weekly aggregation - just a simple list

5. **Create Walk List Item Composable**
   - Row layout with three sections:
     - Left: Walk ID (#X)
     - Middle: Distance + Steps (stacked vertically)
     - Right: Day + Date (stacked vertically)
   - Divider line between items
   - Light background for contrast

6. **Update Navigation for History**
   - Add History destination to NavHost
   - Connect bottom navigation History tab

**Deliverable:** History screen displays a simple list of all completed walks with ID, distance, steps, and date. No charts or complex aggregations.

**Verification:**
```bash
# Complete 3-5 walks over different days
# Open History screen via bottom navigation
# Verify all walks appear in reverse chronological order
# Verify walk IDs increment (#1, #2, #3...)
# Verify distance and steps display correctly
# Verify dates show day of week + date
# Verify list is scrollable if many walks
```

---

## 4. Key Technical Decisions

| Decision | Rationale |
|----------|-----------|
| **Jetpack Compose** | Modern declarative UI, reduces boilerplate, official recommendation from Google |
| **Room ORM** | Type-safe database access, compile-time query verification, Flow support |
| **StateFlow** | Reactive data binding ensures UI reflects latest state |
| **Hilt DI** | Reduces boilerplate, enables easy testing, Google-recommended |
| **Foreground Service** | Guarantees background tracking even if app is backgrounded |
| **Built-in Step Counter** | More accurate and battery-efficient than accelerometer-based detection |
| **Stride-Based Distance** | Simple calculation (steps × stride length), no GPS needed |
| **DataStore** | Type-safe, coroutines-based, officially recommended for settings |
| **No GPS Tracking** | Simplifies implementation, reduces battery drain, removes permission complexity |
| **No Path Visualization** | Removes dependency on Google Maps SDK, simplifies architecture |
| **Simple List History** | Easier to implement than charts, sufficient for project requirements |

---

## 5. Dependencies Overview

### Core Dependencies (Compose & Android)
- Jetpack Compose BOM 2024.09.00
- Material 3 for Compose
- AndroidX Core KTX 1.17.0
- Activity Compose 1.8.0

### Architecture Components
- Lifecycle ViewModel Compose
- Room Database 2.6.1
- DataStore Preferences 1.0.0
- Hilt (Dagger) 2.48

### Testing
- JUnit 4.13.2
- Espresso 3.5.1
- Compose UI Test

**Removed Dependencies (No longer needed):**
- ❌ Play Services Maps
- ❌ Play Services Location
- ❌ MPAndroidChart

---

## 6. Testing Strategy

### Unit Tests (Fast, No Android Framework)

**What to test:**
- Domain models (Walk)
- Use cases (StartWalkUseCase, StopWalkUseCase)
- StrideCalculator utility
- ViewModel logic

**Example test targets:**
- StrideCalculator: Verify stride = height × 0.43 / 100
- StartWalkUseCase: Verify walk is created with isActive = true
- HomeViewModel: Verify timer increments every second

**Location:** `app/src/test/`

### Instrumented Tests (Requires Device/Emulator)

**What to test:**
- Repository implementations
- Room DAO operations
- DataStore persistence
- End-to-end flows

**Example test targets:**
- WalkRepository.startWalk() inserts record to database
- WalkDao.getAllWalks() returns correct list
- SettingsRepository saves and loads user height correctly

**Location:** `app/src/androidTest/`

---

## 7. Performance Considerations

### Database Optimization
1. **Indexing:** Add index on `WalkEntity.date` for faster queries
2. **Background Threads:** All database operations on IO dispatcher
3. **Flow Collection:** Use `.collectAsState()` in Compose

### Sensor Optimization
1. **Step Counter Throttling:** Update UI every second, not on every sensor event
2. **Service Lifecycle:** Stop sensors immediately when walk ends

### Compose Performance
1. **Lazy Loading:** Use LazyColumn for walk lists
2. **State Hoisting:** Keep state in ViewModels
3. **Stable Keys:** Use walk.id as keys in LazyColumn

---

## 8. Permission Handling

### Required Permissions
- **ACTIVITY_RECOGNITION:** Step counter sensor (API 29+)
- **FOREGROUND_SERVICE:** Background tracking service
- **POST_NOTIFICATIONS:** Show foreground service notification (API 33+)

### Permission Request Flow
1. **On App Launch:** Check if permissions are granted
2. **Before Starting Walk:** Request permissions if not granted
3. **Handle Denial:** Show explanation dialog, disable features gracefully

---

## 9. Error Handling Strategy

### Database Errors
- Use `fallbackToDestructiveMigration()` during development
- Handle null results gracefully (empty list, default values)

### Sensor Errors
- Check if device has step counter, show warning if missing
- Disable sensor features if permission denied

### User Input Errors
- Validate height input (50-250 cm range)
- Show error messages for invalid input

---

## 10. Development Timeline

### Week 1: Foundation
- **Days 1-2:** Phase 1 (Foundation & Home Screen) - Database + basic UI
- **Days 3-4:** Phase 2 (Settings Screen) - User preferences
- **Day 5:** Testing and bug fixes

### Week 2: Sensors
- **Days 1-3:** Phase 3 (Sensor Integration) - Step counting
- **Days 4-5:** Testing on real device, permission flows

### Week 3: History
- **Days 1-2:** Phase 4 (History Screen) - Simple list view
- **Days 3-5:** Polish UI, testing, bug fixes

### Week 4 (Buffer)
- Final end-to-end testing
- Fix critical bugs
- Prepare presentation materials

**Total Estimated Time:** 3-4 weeks

---

## 11. What We Removed (Simplifications)

To make this project manageable for a high school final project, we removed:

1. **GPS Tracking**
   - No GpsPointEntity in database
   - No GpsLocationManager service
   - No location permissions needed
   - Distance calculated purely from stride length

2. **Path Visualization**
   - No Paths screen
   - No Google Maps SDK integration
   - No map thumbnails or polylines

3. **Complex Charts**
   - No MPAndroidChart library
   - No bar charts in History
   - No weekly aggregation
   - Simple list view only

4. **Complex Distance Calculation**
   - No Haversine formula
   - No GPS-based distance calculation
   - Only stride-based: steps × (height × 0.43 / 100)

5. **Navigation Complexity**
   - Settings moved to bottom nav (simpler than gear icon pattern)
   - Only 3 screens total (Home, History, Settings)

---

## 12. Definition of Done

A phase is considered **complete** when:

1. ✅ All code compiles without errors
2. ✅ All unit tests pass
3. ✅ Feature works on physical Android device (not just emulator)
4. ✅ Deliverable is achieved (as stated in phase description)
5. ✅ Verification steps succeed
6. ✅ Code is committed to version control with clear commit message
7. ✅ No critical bugs blocking next phase

---

## 13. Key Formulas

### Stride Length Calculation
```kotlin
fun calculateStrideLength(heightCm: Int): Double {
    return (heightCm * 0.43) / 100.0  // Returns meters
}
```

### Distance Calculation
```kotlin
fun calculateDistance(steps: Int, heightCm: Int): Double {
    val strideLengthMeters = calculateStrideLength(heightCm)
    val distanceMeters = steps * strideLengthMeters
    return distanceMeters / 1000.0  // Convert to kilometers
}
```

---

## Conclusion

This simplified plan provides a **structured, phase-based approach** to building StepEeeasy with Jetpack Compose, optimized for a high school final project.

**Key Success Factors:**
- Follow phases in order (don't skip ahead)
- Test each phase before moving to the next
- Use real Android device for sensor testing
- Commit code after each completed phase
- Keep it simple - you can always add features later

**Remember:** This is a high school project - prioritize learning and working code over complexity. The simplified architecture is much more maintainable and still demonstrates solid software engineering principles.

---
