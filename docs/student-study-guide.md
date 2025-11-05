# StepEeeasy - Student Study Guide

**Purpose:** This guide helps you understand your walking tracker app so you can explain it to your teacher. No complicated jargon - just clear explanations.

---

## Quick Kotlin for Java Programmers

If you know Java, here's what's different in Kotlin:

| Java | Kotlin | What it means |
|------|--------|---------------|
| `public void doSomething()` | `fun doSomething()` | Function declaration |
| `public String name;` | `val name: String` | Variable (val = final, var = mutable) |
| `new User()` | `User()` | Create object (no "new") |
| `user.getName()` | `user.name` | Access property (no getter needed) |
| `if (x != null) { x.doSomething(); }` | `x?.doSomething()` | Safe null check with `?.` |
| `String name = user != null ? user.name : "Guest";` | `val name = user?.name ?: "Guest"` | Elvis operator `?:` for defaults |

**Key differences:**
- Variables declared with `val` (like `final`) or `var` (mutable)
- Functions use `fun` keyword
- No semicolons needed
- Null safety built into the language with `?` and `!!`

---

## What Your App Does (User's View)

Your app lets users track their walks:

1. **Home Screen**: Tap START → see timer and step count → tap STOP → walk saved
2. **History Screen**: View list of all past walks with date, time, steps, and distance
3. **Settings Screen**: Enter your height (for calculating distance) and clear all walks

Now let's see **how the code makes this happen**.

---

## App Architecture Overview

Your app follows **Clean Architecture** with 3 main layers:

```
USER TAPS BUTTON
       ↓
[Presentation Layer] ← Shows UI, handles user input
       ↓
[Domain Layer] ← Business logic (use cases)
       ↓
[Data Layer] ← Saves/loads data from database
```

**Why 3 layers?**
- **Separation of concerns**: Each layer has one job
- **Easier to test**: You can test each layer separately
- **Easier to change**: Changing UI doesn't affect database code

---

## Key Technical Concepts (Simple Version)

### 1. ViewModel
**What it is:** A class that holds data for the screen and survives screen rotation.

**Why?** If you store data directly in the screen (Composable), it gets lost when the phone rotates. ViewModel keeps the data safe.

**Example:** `HomeViewModel` holds the timer, step count, and walk state.

### 2. Room Database
**What it is:** A SQLite database wrapper that lets you save data permanently on the phone.

**Why?** When the app closes, you don't lose the walks. Room saves them to the phone's storage.

**Example:** `WalkEntity` is a table that stores walks with columns like `start_time`, `total_steps`, etc.

### 3. Coroutines (suspend functions)
**What it is:** A way to run code without freezing the screen.

**Why?** Database operations are slow. If you run them on the main thread, the app freezes. Coroutines run them in the background.

**How to spot:** Functions marked with `suspend` keyword.

### 4. Flow
**What it is:** A stream of data that updates automatically when the database changes.

**Why?** When you save a new walk, the history screen updates automatically without you having to reload it.

**Example:** `walkDao.getAllWalks()` returns `Flow<List<Walk>>` - a stream of walk lists.

### 5. Dependency Injection (Hilt)
**What it is:** A way to automatically provide objects to classes that need them.

**Why?** Instead of creating objects manually (`new Database()`), Hilt creates and provides them for you.

**How to spot:** `@Inject` annotations and `@HiltViewModel`.

---

## User Journey → Code Flow

### **Journey 1: User Starts a Walk**

**What user sees:**
1. User taps START button on Home screen
2. Screen shows timer starting from 00:00:00
3. Step count updates in real-time
4. Distance calculates automatically

**What code does:**

```
User taps START
     ↓
HomeScreen.kt (line 169)
     ↓
HomeViewModel.onStartWalkClicked() (line 82)
     ↓
StartWalkUseCase() (line 49)
     ↓
WalkRepositoryImpl.startWalk() (line 32)
     ↓
WalkDao.insertWalk() → saves to database
     ↓
StepCounterManager.startTracking() → starts counting steps
     ↓
HomeViewModel.startTimer() → starts clock
```

**Key files:**
- `HomeScreen.kt`: The START button UI
- `HomeViewModel.kt`: Handles button click, manages timer
- `StartWalkUseCase.kt`: Business logic for starting walk
- `WalkRepositoryImpl.kt`: Saves walk to database
- `StepCounterManager.kt`: Reads step sensor

---

### **Journey 2: Counting Steps**

**What user sees:**
- Step count updates every second
- Distance updates automatically

**What code does:**

```
Phone's step sensor detects steps
     ↓
StepCounterManager.onSensorChanged() (line 87)
     ↓
Calculates delta: current_steps - baseline_steps (line 98)
     ↓
Calls callback: onStepCountChanged(steps)
     ↓
HomeViewModel.onStepsUpdated() (line 169)
     ↓
Calculates distance: StrideCalculator.calculateDistanceMeters() (line 172)
     ↓
Updates UI state → screen refreshes
```

**Key concepts:**
- **Sensor baseline**: When walk starts, records current total steps as baseline
- **Delta calculation**: Walk steps = current sensor value - baseline
- **Stride formula**: Distance = steps × (height × 0.43 / 100)

**Example:**
- Your height: 170 cm
- Stride length: 170 × 0.43 ÷ 100 = 0.731 meters
- You walk 1000 steps: 1000 × 0.731 = 731 meters = 0.73 km

---

### **Journey 3: User Stops a Walk**

**What user sees:**
1. User taps STOP button
2. Timer stops
3. Walk saved message appears
4. Screen returns to idle state

**What code does:**

```
User taps STOP
     ↓
HomeViewModel.onStopWalkClicked() (line 124)
     ↓
StepCounterManager.stopTracking() → gets final step count (line 131)
     ↓
StrideCalculator.calculateDistanceMeters() → calculates final distance (line 141)
     ↓
StopWalkUseCase(steps, distance) (line 144)
     ↓
WalkRepositoryImpl.stopWalk() (line 55)
     ↓
Updates database with end_time, total_steps, distance_meters
     ↓
UI changes to Idle state → shows START button again
```

---

### **Journey 4: Viewing History**

**What user sees:**
- List of all walks with date, time, steps, distance
- Newest walks appear first

**What code does:**

```
User taps History tab
     ↓
HistoryScreen displays (HistoryScreen.kt line 28)
     ↓
HistoryViewModel loads walks (HistoryViewModel.kt line 21)
     ↓
GetAllWalksUseCase() (GetAllWalksUseCase.kt line 25)
     ↓
WalkRepositoryImpl.getAllWalks() (WalkRepositoryImpl.kt line 87)
     ↓
WalkDao.getAllWalks() → queries database (WalkDao.kt)
     ↓
Returns Flow<List<Walk>> → automatically updates when data changes
     ↓
HistoryScreen displays list with WalkListItem for each walk
```

**Key concept - Flow:**
- When you start a new walk, History screen updates **automatically**
- No need to refresh manually
- Flow emits new data whenever database changes

---

### **Journey 5: Saving Height in Settings**

**What user sees:**
1. User types height (e.g., "175") in Settings
2. Taps SAVE button
3. Sees "Height saved successfully" message

**What code does:**

```
User types in TextField
     ↓
SettingsViewModel.onHeightChanged() (line 66)
     ↓
User taps SAVE
     ↓
SettingsViewModel.onSaveHeight() (line 74)
     ↓
Validates: must be a number, between 50-250
     ↓
SettingsRepository.saveUserHeight() (SettingsRepositoryImpl.kt line 26)
     ↓
SettingsDataStore saves to disk (SettingsDataStore.kt)
     ↓
Height is now used in stride calculation for all future walks
```

**Key concept - DataStore:**
- Settings use DataStore (not Room database)
- DataStore = simple key-value storage for preferences
- Room = complex tables with relationships for walks

---

## File-by-File Breakdown

### **Presentation Layer** (UI + ViewModels)

#### `MainActivity.kt`
**What it does:** Entry point of the app. Sets up the bottom navigation bar.

**Key concepts:**
- `@AndroidEntryPoint`: Tells Hilt this activity can receive dependencies
- `NavigationSuiteScaffold`: Creates bottom navigation automatically
- `enum class AppDestinations`: Defines the 3 tabs (Home, History, Settings)

#### `presentation/home/HomeScreen.kt`
**What it does:** Displays the Home screen UI.

**Key parts:**
- `IdleContent`: Shows START button when no walk active (line 114)
- `ActiveWalkContent`: Shows timer, steps, distance, STOP button (line 232)
- `StatCard`: Reusable card component for displaying stats (line 305)
- Permission handling: Requests ACTIVITY_RECOGNITION permission before starting (line 123)

#### `presentation/home/HomeViewModel.kt`
**What it does:** Manages Home screen data and logic.

**Key parts:**
- `_uiState`: Holds current screen state (Idle, WalkActive, or Error) (line 57)
- `onStartWalkClicked()`: Handles START button (line 82)
- `onStopWalkClicked()`: Handles STOP button (line 124)
- `startTimer()`: Updates elapsed seconds every 1 second (line 195)
- `onStepsUpdated()`: Updates UI when step sensor reports new steps (line 169)

**Important concept - Sealed class:**
```kotlin
sealed class HomeUiState {
    object Idle : HomeUiState()
    data class WalkActive(...) : HomeUiState()
    data class Error(...) : HomeUiState()
}
```
- Only 3 possible states
- Compiler forces you to handle all cases (no bugs from forgetting a state)

#### `presentation/history/HistoryScreen.kt`
**What it does:** Displays list of all completed walks.

**Key parts:**
- `LazyColumn`: Efficiently displays long lists (only renders visible items) (line 53)
- `EmptyStateMessage`: Shows "No walks yet" when list is empty (line 77)
- `WalkListItem`: Each walk's card in the list (defined in WalkListItem.kt)

#### `presentation/history/HistoryViewModel.kt`
**What it does:** Loads walks for History screen.

**Key parts:**
- `walks`: Flow that automatically updates when database changes (line 21)
- `.stateIn()`: Converts Flow to StateFlow for Compose (keeps last value in memory)

#### `presentation/settings/SettingsScreen.kt`
**What it does:** Displays Settings screen UI.

**Key parts:**
- `HeightInputSection`: TextField for entering height with SAVE button (line 149)
- `ClearWalksConfirmationDialog`: Confirmation before deleting all walks (line 276)
- `AppInfoFooter`: Shows email and version info (line 245)

#### `presentation/settings/SettingsViewModel.kt`
**What it does:** Manages settings state and validation.

**Key parts:**
- `onSaveHeight()`: Validates height (must be number, 50-250) (line 74)
- `onClearWalksClicked()`: Shows confirmation dialog (line 119)
- `onConfirmClearWalks()`: Actually deletes walks after confirmation (line 135)

---

### **Domain Layer** (Business Logic)

#### `domain/model/Walk.kt`
**What it is:** Clean representation of a walk (no database details).

**Key properties:**
- `id`: Unique identifier
- `startTime` / `endTime`: When walk started/ended (LocalDateTime - easy to work with)
- `totalSteps`: Final step count
- `distanceMeters`: Distance in meters
- `durationSeconds`: Calculated property (endTime - startTime)
- `distanceKm`: Calculated property (distanceMeters ÷ 1000)

#### `domain/usecase/StartWalkUseCase.kt`
**What it does:** Encapsulates the "start walk" business action.

**Why use cases?**
- Clear naming: `StartWalkUseCase()` is more readable than `repository.insert()`
- Single responsibility: One use case = one business action
- Easy to test: Can test business logic without UI or database

**Pattern: `operator fun invoke()`**
- Allows calling use case like a function: `startWalkUseCase()` instead of `startWalkUseCase.execute()`
- Kotlin feature for making objects callable

#### `domain/usecase/StopWalkUseCase.kt`
**What it does:** Encapsulates the "stop walk" business action.

**Key validation:**
- Checks steps and distance are not negative (line 46-47)
- Returns null if no active walk to stop

#### `domain/usecase/GetAllWalksUseCase.kt`
**What it does:** Gets all walks from repository, sorted by date (newest first).

#### `domain/usecase/ClearAllWalksUseCase.kt`
**What it does:** Deletes all walks from database.

**Why separate use case for delete?**
- Keeps ViewModels simple
- Can add business logic later (e.g., "can't delete if walk is active")

---

### **Data Layer** (Database & Repositories)

#### `data/local/entity/WalkEntity.kt`
**What it is:** Database table definition (how walks are stored).

**Key concepts:**
- `@Entity(tableName = "walks")`: Creates a table named "walks"
- `@PrimaryKey(autoGenerate = true)`: Auto-incrementing ID
- `@ColumnInfo(name = "start_time")`: Column name in database
- `startTime: Long`: Stored as Unix timestamp (milliseconds since 1970)
- `date: String`: Stored as "YYYY-MM-DD" for easy grouping

**Why Long instead of LocalDateTime?**
- SQLite only supports basic types (numbers, text)
- Repository converts between Long ↔ LocalDateTime

#### `data/local/dao/WalkDao.kt`
**What it is:** Data Access Object - defines database queries.

**Key queries:**
- `@Insert`: Adds new walk to database
- `@Update`: Updates existing walk
- `@Query`: Custom SQL queries
- Returns `Flow<>` for queries that need live updates

**Example:**
```kotlin
@Query("SELECT * FROM walks ORDER BY start_time DESC")
fun getAllWalks(): Flow<List<WalkEntity>>
```

#### `data/local/database/AppDatabase.kt`
**What it is:** Main database class. Room generates the implementation automatically.

**Key concepts:**
- `@Database(entities = [WalkEntity::class])`: Lists all tables
- `version = 2`: Schema version (incremented when you change table structure)
- `abstract fun walkDao()`: Room implements this automatically

#### `data/repository/WalkRepositoryImpl.kt`
**What it does:** Translates between database format (WalkEntity) and domain format (Walk).

**Key functions:**
- `startWalk()`: Creates new WalkEntity, inserts into DB, returns Walk (line 32)
- `stopWalk()`: Updates active walk with final values (line 55)
- `getAllWalks()`: Returns Flow of walks, converting from entities (line 87)

**Important pattern - Mapper functions:**
```kotlin
private fun WalkEntity.toDomainModel(): Walk {
    // Convert database format → domain format
    // Long → LocalDateTime
    // String → LocalDate
}
```

**Why mapping?**
- Database uses primitive types (Long, String)
- Domain uses nice types (LocalDateTime, LocalDate)
- Repository hides this complexity

#### `data/local/datastore/SettingsDataStore.kt`
**What it does:** Saves settings (height, permissions) using DataStore.

**Why DataStore instead of Room?**
- Settings = simple key-value pairs
- Room = complex relational data with queries

#### `data/repository/SettingsRepositoryImpl.kt`
**What it does:** Saves/loads settings with validation.

**Key validation (line 26):**
- Height must be between 50-250 cm
- Returns `Result.failure()` if invalid, `Result.success()` if valid

---

### **Service Layer** (Sensors)

#### `service/StepCounterManager.kt`
**What it does:** Manages the phone's built-in step counter sensor.

**How it works:**
1. Phone's step sensor counts **total steps since last reboot**
2. When walk starts, record baseline = current total (line 92-94)
3. Calculate walk steps = current total - baseline (line 98)
4. Call callback with updated step count (line 101)

**Key concepts:**
- `SensorEventListener`: Interface for receiving sensor updates
- `onSensorChanged()`: Called when sensor reports new value (line 87)
- `SENSOR_DELAY_UI`: How often to get updates (good for UI, not too fast)

**Example:**
- Phone total steps: 5000 (baseline when walk starts)
- You walk 100 steps
- Phone total: 5100
- Walk steps: 5100 - 5000 = 100 ✓

---

### **Utilities**

#### `util/StrideCalculator.kt`
**What it does:** Calculates distance from steps and height.

**Formula:**
```
stride_length_meters = height_cm × 0.43 ÷ 100
distance_meters = steps × stride_length_meters
distance_km = distance_meters ÷ 1000
```

**Why 0.43?**
- Research shows average stride length is 43% of height
- Example: 170 cm tall → stride = 73 cm

#### `util/FormatUtils.kt`
**What it does:** Formats numbers for display.

**Examples:**
- `formatSteps(1234)` → "1,234"
- `formatDistanceFromMeters(1234.5)` → "1.23 km"
- Makes numbers look nice in the UI

---

### **Dependency Injection** (Hilt)

#### `di/DatabaseModule.kt`
**What it does:** Tells Hilt how to create database objects.

**Key concept - @Provides:**
```kotlin
@Provides
fun provideDatabase(app: Application): AppDatabase {
    return Room.databaseBuilder(...)
}
```
- Hilt calls this when someone needs AppDatabase
- Creates database once and reuses it

#### `di/RepositoryModule.kt`
**What it does:** Tells Hilt how to create repositories.

**Key concept - @Binds:**
```kotlin
@Binds
abstract fun bindWalkRepository(
    impl: WalkRepositoryImpl
): WalkRepository
```
- When someone needs `WalkRepository`, give them `WalkRepositoryImpl`
- Allows swapping implementations (useful for testing)

---

## Common Questions Teachers Ask

### 1. "Why do you need ViewModels?"
**Answer:** ViewModels survive screen rotation. If you store data in the Composable (UI), it gets lost when the phone rotates. ViewModel keeps it safe.

### 2. "What's the difference between val and var?"
**Answer:**
- `val` = final variable (can't change after assignment)
- `var` = mutable variable (can change)

### 3. "Why use Room instead of SQLite directly?"
**Answer:** Room provides compile-time checking of SQL queries. If you write a wrong query, you get an error before running the app. Raw SQLite only errors at runtime.

### 4. "What's a suspend function?"
**Answer:** A function that can be paused and resumed without blocking the thread. Used for long operations (database, network) so the UI doesn't freeze.

### 5. "Why three layers (Presentation, Domain, Data)?"
**Answer:** Separation of concerns. Each layer has one job:
- Presentation: Show UI
- Domain: Business logic
- Data: Save/load data

Changing one layer doesn't break the others.

### 6. "How does the step counter work?"
**Answer:** The phone has a built-in sensor that counts total steps since last reboot. We record the baseline when walk starts, then calculate delta: current - baseline = walk steps.

### 7. "How do you calculate distance without GPS?"
**Answer:** Using the stride length formula:
- Stride ≈ 43% of height
- Distance = steps × stride
- Example: 175 cm tall, 10,000 steps = 7.5 km

### 8. "What's dependency injection?"
**Answer:** Instead of creating objects manually (`new Database()`), Hilt creates and provides them automatically. This makes code easier to test and maintain.

### 9. "Why Flow instead of just getting data once?"
**Answer:** Flow automatically updates when data changes. When you save a new walk, the History screen updates without you having to reload it manually.

### 10. "Why validate height in SettingsRepository instead of ViewModel?"
**Answer:** Business rules belong in the data/domain layer, not UI layer. This way, if you add another way to save height (e.g., API), validation stays consistent.

---

## Quick Reference: Where to Find Things

**"Where is the timer logic?"**
→ `HomeViewModel.kt`, line 195 (`startTimer()`)

**"Where are steps counted?"**
→ `StepCounterManager.kt`, line 87 (`onSensorChanged()`)

**"Where is distance calculated?"**
→ `StrideCalculator.kt`, line 55 (`calculateDistanceMeters()`)

**"Where are walks saved to database?"**
→ `WalkRepositoryImpl.kt`, line 32 (`startWalk()`) and line 55 (`stopWalk()`)

**"Where is the database table defined?"**
→ `WalkEntity.kt`, line 7-29

**"Where is the START button?"**
→ `HomeScreen.kt`, line 168-182

**"Where is height validation?"**
→ `SettingsRepositoryImpl.kt`, line 26-36

---

## Testing the App

To test on a real phone:

1. **Connect phone via USB**
2. **Enable Developer Mode** (tap Build Number 7 times)
3. **Enable USB Debugging** in Developer Options
4. **Run:** `./gradlew installDebug`
5. **Test walk:**
   - Tap START
   - Actually walk around (step sensor needs real movement)
   - Watch steps and distance update
   - Tap STOP
   - Check History screen for saved walk

**Troubleshooting:**
- **Steps not counting?** Check that ACTIVITY_RECOGNITION permission is granted
- **Distance is 0?** Make sure you saved your height in Settings
- **App crashes?** Check logcat for errors

---

## Key Takeaways

1. **Clean Architecture** separates UI, business logic, and data storage
2. **ViewModel** keeps data safe during screen rotation
3. **Room** saves walks permanently in local database
4. **Coroutines** prevent UI freezing during slow operations
5. **Flow** automatically updates UI when data changes
6. **Step counter sensor** is built into the phone (better than manual detection)
7. **Stride calculation** (43% of height) converts steps to distance
8. **Hilt** automatically creates and provides objects (dependency injection)

---

**Good luck with your presentation! 🎓**

If the teacher asks something you don't know, it's okay to say "I understand the general concept but would need to review the specific implementation details." Honesty is better than guessing.
