# StepEasy - App Overview

StepEasy is an offline-first mobile walking tracker that records individual walks with real-time step counting, distance, and elevation tracking. Users manually start, pause, and stop walk sessions from the Home screen, while the History screen aggregates all walks into a weekly bar chart view. The app includes a visual Paths screen showing map previews of recorded routes, and a Settings screen for configuring height-based stride estimation and sensor permissions. All data is stored locally on the device, ensuring complete privacy and offline functionality.

## Technical Stack

- **Framework:** Ionic Vue with Capacitor
- **UI Components:** Ionic UI Components
- **Target Platform:** Optimized for Android

---

## 📱 App Features

### 1. **Home Screen**

* Displays **today's date** and day of the week.
* Shows **"Active Walk"** status with a **walk duration timer** (HH:MM:SS format).
* Tracks **one walk at a time** — shows _Steps in Current Walk_, _Distance_, and _Elevation_.
* User manually controls session with **Start**, **Pause**, and **Stop** buttons.
* Includes **"View previous walks →"** link to navigate to Paths screen.
* When a walk ends, data resets for the next session.
* Works **offline** and shows live updates while walking.

---

### 2. **History Screen**

* Shows a **weekly bar chart** with totals aggregated per day.
* Each bar represents **total steps and distance** from all walks on that date.
* Navigation arrows allow browsing between weeks.
* Tapping a bar reveals exact totals for that day.

---

### 3. **Paths Screen**

* Displays a **list of recorded walks**, each with:

    * A **small map preview** of the path
    * **Date**, **distance**, and **elevation gain**

* Lets users view their walk history visually.

---

### 4. **Settings Screen**

* **Height input** for stride-based distance estimation

    * _Note:_ app automatically estimates stride from height.

* **Activity Recognition** permission control (sensor access).
* **Clear Recorded Walks** button.
* **Battery optimization note** (informational only).
* **Contact info:** `zerodawn57027@gmail.com`
* Footer: _"App data stored locally only. Version 1.0 (Offline)"_

---