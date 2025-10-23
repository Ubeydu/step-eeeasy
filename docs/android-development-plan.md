# StepEeeasy - Android/Kotlin Development Plan v2 (Jetpack Compose)

**Project Type:** High School Final Project
**Target Platform:** Android (API 34+)
**Architecture:** Clean Architecture + Repository Pattern
**UI Framework:** Jetpack Compose + Material Design 3
**Database:** Room ORM with SQLite
**State Management:** ViewModel + StateFlow/LiveData
**Priority:** Home → Settings → History → Paths (Decoupled)

---

## How to Use This Document

This plan is designed to guide implementation in a structured way:

1. **Read this document first** to understand the overall strategy and implementation order
2. **Refer to `docs/technical-reference-compose.md`** when you need code examples and implementation details
3. **Cross-references** use the format: `[→ Section → Subsection]` pointing to locations in the technical reference
4. **Implementation phases** are ordered by priority and dependency

**Legend:**
- `[→ Tech Ref: Data → Database]` = See technical-reference-compose.md, Data Layer section, Database subsection
- **Deliverable** = What you should have working at the end of each phase
- **Verification** = How to test that the phase was completed successfully

---

## 1. Project Overview

### What is StepEeeasy?

StepEeeasy is an **offline-first walking tracker** for Android that records walk sessions with:
- Real-time step counting (using built-in step counter sensor)
- Distance tracking (GPS-based + stride calculation)
- GPS path visualization on interactive maps
- Historical statistics (aggregated daily/weekly data with charts)

### Key Principles

- **Privacy-First:** All data stored locally, no cloud dependencies
- **Offline-First:** Full functionality without internet connection
- **Battery-Efficient:** Uses hardware sensors, not accelerometer polling
- **Clean Architecture:** Separation of concerns for maintainability
- **Testable:** Clear layer boundaries enable unit testing

### Current Project State

The project has been initialized with:
- Android Studio Compose template
- Basic MainActivity with adaptive navigation scaffold
- Material 3 theme with dynamic color support
- Bottom navigation structure (Home, History, Paths)
- Room database with WalkEntity and GpsPointEntity
- Repository pattern with Hilt dependency injection

**Next Steps:** Follow the implementation roadmap below to build out the full feature set.

---

## 2. Architecture Overview

### Layer Structure

```
┌─────────────────────────────────────────────────────────┐
│  Presentation Layer (UI)                                 │
│  - Composables (Screens)                                 │
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
│   ├── home/              # Home screen composables + ViewModel (includes Settings gear icon)
│   ├── settings/          # Settings screen composables + ViewModel (accessed via Home)
│   ├── history/           # History screen composables + ViewModel (bottom nav)
│   ├── paths/             # Paths screen composables + ViewModel (bottom nav)
│   ├── navigation/        # Navigation setup (NavHost, routes)
│   └── theme/             # Compose theme (colors, typography)
├── service/               # Background Services
│   ├── WalkTrackingService.kt
│   ├── SensorManager.kt
│   └── GpsLocationManager.kt
├── util/                  # Utilities
└── MainActivity.kt
```

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
   `[→ Tech Ref: Dependency Injection → Hilt Setup]`
   - Configure application class with @HiltAndroidApp
   - Add Hilt dependencies to build.gradle.kts
   - Create AppModule for core dependencies

2. **Create Room Database Schema**
   `[→ Tech Ref: Data → Database]`
   - Define WalkEntity (id, start_time, end_time, total_steps, distance_meters, is_active, date)
   - Define GpsPointEntity (id, walk_id, latitude, longitude, timestamp, accuracy)
   - Create WalkDao and GpsPointDao interfaces
   - Create AppDatabase class
   - Note: GPS points enable path visualization; no elevation tracking in MVP

3. **Implement WalkRepository**
   `[→ Tech Ref: Data → Repositories → WalkRepository]`
   - Implement IWalkRepository interface
   - Add startWalk(), stopWalk(), getActiveWalk() methods
   - Handle entity-to-domain model mapping

4. **Create Domain Use Cases**
   `[→ Tech Ref: Domain → Use Cases]`
   - StartWalkUseCase
   - StopWalkUseCase

5. **Build HomeViewModel**
   `[→ Tech Ref: Presentation → Home Screen → HomeViewModel]`
   - Manage walk state (idle, active)
   - Implement timer logic (count elapsed seconds)
   - Expose UI state as StateFlow

6. **Create Home Screen Composable**
   `[→ Tech Ref: Presentation → Home Screen → HomeScreen]`
   - Top app bar with "Active Walk" title and Settings gear icon (⚙️)
   - Display START/STOP button
   - Show timer display (HH:MM:SS format)
   - Show step count (placeholder: 0)
   - Show distance (placeholder: 0.0 km)
   - Show current date
   - "View previous walks →" button (navigates to History)
   - Collect ViewModel state and trigger recomposition

7. **Update Navigation**
   `[→ Tech Ref: Presentation → Navigation]`
   - Configure NavHost with Home destination
   - Update bottom navigation (Home, History, Paths - 3 tabs only)
   - Add Settings icon (gear) to Home screen top app bar
   - Settings accessed via navigation, not bottom tabs

**Deliverable:** Users can tap "START", see a live timer counting up, and tap "STOP". Walk data is persisted to database. No sensor integration yet.

**Verification:**
```bash
# Start the app, tap START, wait 30 seconds, tap STOP
# Check database with:
adb shell
run-as com.example.stepeeeasy
cd databases
sqlite3 stepeeasy.db
SELECT * FROM walks;
# Should see one record with correct timestamps
```

---

### Phase 2: Settings Screen (Week 1-2)

**Goal:** Persistent user preferences that affect stride calculations

#### Tasks

1. **Create Settings DataStore**
   `[→ Tech Ref: Data → DataStore]`
   - Define preferences keys (user_height, activity_recognition_enabled)
   - Create SettingsDataStore wrapper

2. **Implement SettingsRepository**
   `[→ Tech Ref: Data → Repositories → SettingsRepository]`
   - Save/load user height
   - Save/load activity recognition preference
   - Expose settings as Flow

3. **Create SettingsViewModel**
   `[→ Tech Ref: Presentation → Settings Screen → SettingsViewModel]`
   - Manage settings state
   - Handle user height updates
   - Handle clear walks action

4. **Build Settings Screen Composable**
   `[→ Tech Ref: Presentation → Settings Screen → SettingsScreen]`
   - Height input field (TextField with numeric keyboard)
   - Activity Recognition toggle (Switch)
   - "Clear Recorded Walks" button with confirmation dialog
   - Footer with app info

5. **Add Clear Walks Use Case**
   `[→ Tech Ref: Domain → Use Cases → ClearAllWalksUseCase]`
   - Call repository.deleteAllWalks()

6. **Update Navigation for Settings**
   `[→ Tech Ref: Presentation → Navigation]`
   - Add Settings destination to NavHost
   - Add gear icon button to Home screen top app bar
   - Settings screen uses standard back arrow navigation to return

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

**Goal:** Live step counting and GPS tracking during walks

#### Tasks

1. **Request Runtime Permissions**
   `[→ Tech Ref: Configuration → Permissions]`
   - Add permissions to AndroidManifest.xml
   - Implement permission request flow in Home screen
   - Handle permission denial gracefully

2. **Implement SensorManager**
   `[→ Tech Ref: Service → SensorManager]`
   - Register step counter sensor listener
   - Track steps since walk started (baseline offset)
   - Expose step count via callback

3. **Implement GpsLocationManager**
   `[→ Tech Ref: Service → GpsLocationManager]`
   - Use FusedLocationProviderClient
   - Request location updates every 5 seconds
   - Filter low-accuracy points
   - Persist GPS points to database

4. **Create WalkTrackingService**
   `[→ Tech Ref: Service → WalkTrackingService]`
   - Foreground service with persistent notification
   - Orchestrate SensorManager and GpsLocationManager
   - Update notification with live step count
   - Handle START/STOP intents

5. **Integrate StrideCalculator**
   `[→ Tech Ref: Utilities → StrideCalculator]`
   - Calculate stride length from user height
   - Calculate distance from steps × stride length

6. **Integrate DistanceCalculator**
   `[→ Tech Ref: Utilities → DistanceCalculator]`
   - Haversine formula for GPS-based distance
   - Use GPS distance if available, otherwise use stride-based

7. **Update HomeViewModel**
   `[→ Tech Ref: Presentation → Home Screen → HomeViewModel]`
   - Start/stop WalkTrackingService
   - Observe active walk from repository
   - Display real steps and distance
   - Update timer based on walk duration

8. **Update Home Screen UI**
   `[→ Tech Ref: Presentation → Home Screen → HomeScreen]`
   - Show real-time step count
   - Show real-time distance (km)
   - Show live timer

**Deliverable:** When user starts a walk, the app counts steps using hardware sensor, tracks GPS location, calculates distance, and displays all data in real-time. Background tracking continues even when app is minimized.

**Verification:**
```bash
# Grant permissions, tap START
# Walk around with phone (outdoors for GPS)
# Verify step count increases
# Verify distance increases
# Minimize app - notification should show live step count
# Return to app after 2 minutes, verify timer is accurate
# Tap STOP, check database for GPS points
```

---

### Phase 4: History Screen (Week 3)

**Goal:** Visualize historical walk data with weekly charts

#### Tasks

1. **Create DailyStats Domain Model**
   `[→ Tech Ref: Domain → Models → DailyStats]`
   - date, totalSteps, totalDistanceMeters, walkCount

2. **Implement GetDailyStatsUseCase**
   `[→ Tech Ref: Domain → Use Cases → GetDailyStatsUseCase]`
   - Aggregate walks by date
   - Sum steps and distance for each day

3. **Add Query Methods to WalkDao**
   `[→ Tech Ref: Data → Database → WalkDao]`
   - getWalksByDateRange(startDate, endDate)
   - Query for aggregated daily stats

4. **Integrate MPAndroidChart Library**
   `[→ Tech Ref: Configuration → Dependencies]`
   - Add dependency to build.gradle.kts
   - Configure chart styling for Material 3

5. **Create HistoryViewModel**
   `[→ Tech Ref: Presentation → History Screen → HistoryViewModel]`
   - Load weekly data (Sunday to Saturday)
   - Handle week navigation (previous/next)
   - Format data for chart display

6. **Build History Screen Composable**
   `[→ Tech Ref: Presentation → History Screen → HistoryScreen]`
   - Week navigation arrows (< Week >)
   - Bar chart showing daily steps (primary metric)
   - Tooltip on bar tap shows: day name, steps, and distance (e.g., "Sunday, 7,860 steps, 5.8 km")
   - Date range display (week of October 12 - 18, 2025)

7. **Create Walk List Item Composable**
   `[→ Tech Ref: Presentation → History Screen → WalkListItem]`
   - Show walk duration, steps, distance
   - Tap to navigate to path detail (Phase 5)

8. **Update Navigation for History**
   `[→ Tech Ref: Presentation → Navigation]`
   - Add History destination to NavHost
   - Update bottom navigation

**Deliverable:** History screen displays a bar chart of steps per day for the current week. Users can navigate between weeks. Tapping a day shows all walks from that day in a list.

**Verification:**
```bash
# Complete 2-3 walks over different days
# Open History screen
# Verify chart shows bars for days with walks
# Tap on a bar, verify walks list appears
# Navigate to previous week, verify chart updates
```

---

### Phase 5: Paths Screen (Week 4)

**Goal:** Visual map representation of all recorded walks (individual entries, not grouped by day)

#### Tasks

1. **Integrate Google Maps SDK**
   `[→ Tech Ref: Configuration → Dependencies]`
   - Add Maps dependency
   - Add API key to AndroidManifest.xml
   - Configure ProGuard rules if needed

2. **Create GetAllWalksUseCase**
   `[→ Tech Ref: Domain → Use Cases → GetAllWalksUseCase]`
   - Fetch all walks with GPS points
   - Already returns List<Walk> sorted by start time (most recent first)

3. **Create PathsViewModel**
   `[→ Tech Ref: Presentation → Paths Screen → PathsViewModel]`
   - Load all walks using GetAllWalksUseCase
   - Expose as StateFlow<List<Walk>>
   - Handle walk selection for full-screen map detail view

4. **Build Paths Screen Composable**
   `[→ Tech Ref: Presentation → Paths Screen → PathsScreen]`
   - LazyColumn of individual walk cards (one card per walk, NOT grouped by day)
   - Each card shows:
     - Date + Time: "October 12, 2025, 16:50" (format: "MMMM d, yyyy, HH:mm")
     - Distance: "5.8 km"
     - Map thumbnail with GPS path polyline
   - Multiple walks on same day are listed separately with different times
   - Tap card to expand to full-screen map view

5. **Create Path Map Composable**
   `[→ Tech Ref: Presentation → Paths Screen → PathMapComposable]`
   - GoogleMap Composable integration
   - Draw polyline from GPS points (walk.gpsPoints)
   - Show start/end markers
   - Camera auto-zoom to fit path bounds

6. **Implement Lazy Loading**
   - Only load map for visible cards
   - Unload maps when scrolled out of view
   - Use rememberSaveable for scroll position

7. **Update Navigation for Paths**
   `[→ Tech Ref: Presentation → Navigation]`
   - Add Paths destination to NavHost (already in bottom nav from Phase 1)

**Deliverable:** Paths screen displays a scrollable list of all recorded walks as individual entries (with date + time). Each walk shows its own map thumbnail with the specific GPS path. Tapping a walk expands to full-screen map view.

**Verification:**
```bash
# Complete 2-3 walks with GPS enabled (do multiple walks on same day to test)
# Open Paths screen
# Verify all walks appear as separate entries (not grouped)
# Verify each entry shows date + time (e.g., "October 12, 2025, 16:50")
# Verify maps show correct paths for each walk
# Verify start/end markers are visible
# Tap a walk, verify full-screen map appears
# Verify multiple walks on same day are listed separately
```

---

## 4. Key Technical Decisions

| Decision | Rationale | Reference |
|----------|-----------|-----------|
| **Jetpack Compose** | Modern declarative UI, reduces boilerplate, official recommendation from Google. Better state management than XML. | [→ Tech Ref: Presentation] |
| **Room ORM** | Type-safe database access, compile-time query verification, Flow support for reactive updates. | [→ Tech Ref: Data → Database] |
| **StateFlow + LiveData** | Reactive data binding ensures UI reflects latest state. Handles configuration changes automatically. | [→ Tech Ref: Presentation → ViewModels] |
| **Hilt DI** | Reduces boilerplate, enables easy testing, Google-recommended approach. Better than manual DI. | [→ Tech Ref: DI] |
| **Foreground Service** | Guarantees background tracking even if app is backgrounded. Required for continuous GPS/step tracking. | [→ Tech Ref: Service → WalkTrackingService] |
| **Built-in Step Counter** | More accurate and battery-efficient than accelerometer-based detection. Uses low-power co-processor. | [→ Tech Ref: Service → SensorManager] |
| **GPS for Distance** | More accurate than stride-based dead reckoning. Enables path visualization. Falls back to stride calculation when GPS unavailable. | [→ Tech Ref: Service → GpsLocationManager] |
| **DataStore over SharedPreferences** | Type-safe, coroutines-based, officially recommended replacement for SharedPreferences. | [→ Tech Ref: Data → DataStore] |
| **Clean Architecture** | Separation of concerns makes code testable and maintainable. Easy to swap implementations. | [→ Tech Ref: All sections] |

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

### Sensors & Location
- Play Services Maps 18.2.0
- Play Services Location 21.0.1

### Charts & Visualization
- MPAndroidChart v3.1.0

### Testing
- JUnit 4.13.2
- Espresso 3.5.1
- Compose UI Test

**Full dependency configuration:** `[→ Tech Ref: Configuration → Dependencies]`

---

## 6. Testing Strategy

### Unit Tests (Fast, No Android Framework)

**What to test:**
- Domain models (Walk, GpsPoint, DailyStats)
- Use cases (StartWalkUseCase, StopWalkUseCase)
- Utilities (DistanceCalculator, StrideCalculator, DateFormatter)
- ViewModel logic (business rules, state transformations)

**Example test targets:**
- DistanceCalculator: Verify Haversine formula accuracy
- StrideCalculator: Verify stride = height × 0.43
- StartWalkUseCase: Verify walk is created with isActive = true
- HomeViewModel: Verify timer increments every second

**Location:** `app/src/test/`

### Instrumented Tests (Requires Device/Emulator)

**What to test:**
- Repository implementations (WalkRepository, SettingsRepository)
- Room DAO operations (insert, query, delete)
- DataStore persistence
- End-to-end flows (start walk → insert to DB → query active walk)

**Example test targets:**
- WalkRepository.startWalk() inserts record to database
- WalkDao.getActiveWalk() returns correct walk
- SettingsRepository saves and loads user height correctly

**Location:** `app/src/androidTest/`

### Compose UI Tests

**What to test:**
- Composable rendering (button visible, text displays)
- User interactions (tap button, enter text)
- Navigation flows (home → settings → back)
- State updates trigger recomposition

**Example test targets:**
- HomeScreen shows START button when no active walk
- HomeScreen shows STOP button when walk is active
- SettingsScreen height field updates on text input

**Location:** `app/src/androidTest/`

**Testing examples:** `[→ Tech Ref: Testing]`

---

## 7. Performance Considerations

### Database Optimization
1. **Indexing:** Add index on `WalkEntity.date` for faster date range queries
2. **Batch Inserts:** Insert GPS points in batches of 10+ to reduce transactions
3. **Flow Collection:** Use `.collectAsState()` in Compose instead of repeated `.first()` calls
4. **Background Threads:** All database operations on IO dispatcher (Room handles this automatically with suspend functions)

### Sensor & GPS Optimization
1. **Step Counter Throttling:** Update UI every second, not on every sensor event
2. **GPS Filtering:** Reject points with accuracy > 50 meters
3. **Location Update Interval:** 5 seconds is balanced (longer = less accurate, shorter = more battery drain)
4. **Service Lifecycle:** Stop sensors immediately when walk ends

### Compose Performance
1. **Lazy Loading:** Use LazyColumn for walk lists (don't render all items at once)
2. **State Hoisting:** Keep state in ViewModels, not in Composables
3. **Derived State:** Use `derivedStateOf` for computed values to avoid unnecessary recomposition
4. **Stable Keys:** Use stable keys in LazyColumn items (walk.id) for efficient recomposition
5. **Map Optimization:** Only render maps for visible items in Paths screen

### Memory Management
1. **Coroutine Scope Cancellation:** Cancel all coroutines in ViewModel.onCleared()
2. **Flow Lifecycle:** Collect flows with `repeatOnLifecycle(STARTED)` to stop when backgrounded
3. **Bitmap Caching:** Cache map thumbnails in memory with LruCache (if generating thumbnails)

---

## 8. Permission Handling

### Required Permissions
- **ACCESS_FINE_LOCATION:** GPS tracking
- **ACCESS_COARSE_LOCATION:** Coarse location (required alongside fine)
- **ACTIVITY_RECOGNITION:** Step counter sensor (API 29+)
- **FOREGROUND_SERVICE:** Background tracking service
- **FOREGROUND_SERVICE_LOCATION:** Location access in foreground service (API 34+)
- **POST_NOTIFICATIONS:** Show foreground service notification (API 33+)

### Permission Request Flow
1. **On App Launch:** Check if permissions are granted
2. **Before Starting Walk:** Request permissions if not granted
3. **Handle Denial:** Show explanation dialog, gracefully disable features
4. **Settings Link:** Provide button to open app settings if user permanently denies

**Implementation details:** `[→ Tech Ref: Configuration → Permissions]`

---

## 9. Error Handling Strategy

### Database Errors
- **Room Migration Failures:** Use `fallbackToDestructiveMigration()` during development
- **Insert Failures:** Catch exceptions in repository, return Result type to ViewModel
- **Query Failures:** Handle null results gracefully (empty list, default values)

### Sensor Errors
- **Sensor Not Available:** Check if device has step counter, show warning if missing
- **Permission Denied:** Disable sensor features, show explanation to user
- **GPS Unavailable:** Fall back to stride-based distance calculation

### Network Errors
- **Maps API Failure:** Show placeholder map or error message
- **No Internet:** App should work fully offline (maps may not load, but core features work)

### User Input Errors
- **Invalid Height:** Validate input (50-250 cm range), show error message
- **Empty Fields:** Disable save button until valid input

**Error handling patterns:** `[→ Tech Ref: Common Patterns → Error Handling]`

---

## 10. Future Enhancements (Post-MVP)

These features are **out of scope** for the initial release but can be added later:

### Tier 1 (Next Release)
- [ ] **Pause/Resume Walks:** Allow pausing mid-walk (useful for breaks)
- [ ] **Average Pace:** Show minutes per kilometer during active walk

### Tier 2 (Future Releases)
- [ ] **Dark/Light Theme Toggle:** Manual override for system theme
- [ ] **Export Walk Data:** Export walks as GPX/KML files
- [ ] **Social Sharing:** Share walk stats as image to social media
- [ ] **Calorie Estimation:** Calculate calories burned based on distance and user weight

### Tier 3 (Advanced)
- [ ] **Route Editing:** Manually trim start/end of GPS path
- [ ] **Achievements/Badges:** Gamification (first 10k steps, longest walk, etc.)
- [ ] **Wear OS Sync:** Connect to smartwatch for heart rate and more accurate tracking
- [ ] **Cloud Backup:** Optional Firebase/Google Drive backup

---

## 11. Development Timeline

### Week 1: Foundation
- **Days 1-2:** Phase 1 (Foundation & Home Screen) - Database + basic UI
- **Days 3-4:** Phase 2 (Settings Screen) - User preferences
- **Day 5:** Testing and bug fixes

### Week 2: Sensors
- **Days 1-3:** Phase 3 (Sensor Integration) - Step counting + GPS
- **Days 4-5:** Testing on real device, permission flows, bug fixes

### Week 3: History
- **Days 1-3:** Phase 4 (History Screen) - Data aggregation + charts
- **Days 4-5:** Polish UI, add animations, testing

### Week 4: Maps
- **Days 1-3:** Phase 5 (Paths Screen) - Map integration
- **Days 4-5:** Final testing, bug fixes, polish

### Week 5 (Buffer)
- Final end-to-end testing
- Fix critical bugs
- Prepare presentation materials
- Write documentation

**Total Estimated Time:** 4-5 weeks

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

## 13. Getting Help

### When Stuck
1. **Check Technical Reference:** `[→ Tech Ref: Relevant Section]`
2. **Review Original Plan:** Compare with `android-development-plan-v1.md` for XML/Fragment approach if needed
3. **Search Documentation:**
   - Compose: https://developer.android.com/jetpack/compose
   - Room: https://developer.android.com/training/data-storage/room
   - Hilt: https://developer.android.com/training/dependency-injection/hilt-android
4. **Test Incrementally:** Don't write entire feature before testing - test each component as you go

### Common Pitfalls
- **Forgetting @Composable annotation** on screen functions
- **Not using rememberSaveable** for state that should survive configuration changes
- **Collecting Flows outside of LaunchedEffect** (causes recomposition loops)
- **Blocking main thread** with synchronous database calls (always use suspend functions)
- **Not requesting permissions** before accessing sensors/GPS
- **Forgetting to update ViewModel state** from background operations

---

## Conclusion

This plan provides a **structured, phase-based approach** to building StepEeeasy with Jetpack Compose. Each phase builds on the previous one, ensuring you always have a working app.

**Key Success Factors:**
- Follow phases in order (don't skip ahead)
- Test each phase before moving to the next
- Refer to technical reference for implementation details
- Use real Android device for sensor testing (emulator has limited sensor support)
- Commit code after each completed phase

**Remember:** This is a high school project - prioritize learning and working code over perfection. The architecture is solid enough to extend in the future.

---