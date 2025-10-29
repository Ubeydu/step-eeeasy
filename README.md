# StepEeeasy - App Overview

StepEeeasy is an **offline-first mobile walking tracker** that records individual walks with real-time step counting and stride-based distance tracking. Users manually start and stop walk sessions from the Home screen, while the History screen displays a simple list of all recorded walks. The Settings screen allows configuring height for accurate stride estimation and managing sensor permissions. All data is stored locally on the device, ensuring complete privacy and offline functionality.

## Technical Stack

- **Framework:** Android Native with Jetpack Compose
- **UI Components:** Material Design 3
- **Target Platform:** Android API 34+

---

## 📱 App Features

### 1. **Home Screen**

* Displays **today's date** and day of the week.
* Shows **"Active Walk"** status with a **walk duration timer** (HH:MM:SS format).
* Tracks **one walk at a time** — shows _Steps in Current Walk_ and _Distance_ (calculated from stride length).
* User manually controls session with **START** and **STOP** buttons.
* Includes **"View previous walks →"** link to navigate to History screen.
* When a walk ends, data is saved and display resets for the next session.
* Works **offline** and shows live updates while walking.
* **Distance calculation:** Steps × stride length (based on user height from Settings)

---

### 2. **History Screen**

* Displays a **simple list** of all recorded walks (most recent first).
* Each walk entry shows:
    * **Walk ID** (#1, #2, #3...)
    * **Distance** in kilometers
    * **Total steps**
    * **Day of week and date** (e.g., "Sunday Oct 18, 2025")
* Clean, scrollable list view - no charts or aggregations.
* Accessible via "View previous walks →" link on Home screen or bottom navigation.

---

### 3. **Settings Screen**

* **Height input** for stride-based distance estimation
    * Formula: `stride_length = height × 0.43`
    * Saved value persists across app restarts
    * Clear distinction between draft input and saved database value
* **Activity Recognition** permission control (sensor access toggle).
* **Clear Recorded Walks** button with confirmation dialog.
* **Battery optimization tip** (informational).
* **Contact info:** `zerodawn57027@gmail.com`
* Footer: _"App data stored locally only. Version 1.0 (Offline)"_

---

## 🧭 Navigation

The app uses a **3-tab bottom navigation bar**:

1. **Home** 🏠 - Start/stop walks, view live tracking
2. **History** 🕐 - Browse all recorded walks
3. **Settings** ⚙️ - Configure height and permissions

All screens are accessible at any time via the bottom navigation.

---

## 🏗️ Architecture

**Pattern:** Clean Architecture + Repository Pattern

**Key Layers:**
- **Presentation:** Jetpack Compose UI + ViewModels (state management)
- **Domain:** Use cases (business logic) + domain models
- **Data:** Room database + DataStore (settings) + repositories

**Database:**
- **WalkEntity** table only (no GPS tracking)
- **DataStore** for user preferences (height, permissions)

**Distance Calculation:**
- **Stride-based:** `distance = steps × (height × 0.43 / 100)`
- No GPS or location services required
- Battery-efficient and works indoors

---

## 📚 Documentation

- **Development Plan:** `docs/android-development-plan-simplified.md`
- **Progress Log:** `docs/progress/2025-10-13_to_10-26.md`
- **Next Steps:** `docs/progress/plan-for-2025-10-29.md`
- **Project Instructions:** `CLAUDE.md`

---

## 📧 Contact

**Developer:** zerodawn57027@gmail.com
**Privacy:** All data stored locally only - completely offline

---