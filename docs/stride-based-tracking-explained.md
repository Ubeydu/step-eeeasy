# Stride-Based Distance Tracking Explained

## How It Works Without GPS

### 1. **Step Counter Sensor (Hardware)**
Modern Android phones have a **built-in step counter sensor** (`Sensor.TYPE_STEP_COUNTER`) that uses:
- **Accelerometer**: Detects motion patterns characteristic of walking
- **Gyroscope**: Measures orientation changes
- **Pattern Recognition**: Phone's hardware can distinguish walking steps from other movements (sitting, driving, etc.)

The sensor **physically counts each step** by detecting the up-down bounce pattern when you walk.

### 2. **Stride Length Estimation (Math)**
- Research shows stride length correlates with height
- Formula: `stride_length = height × 0.43`
- Example: Person 175cm tall → stride ≈ 75cm per step
- Not perfect, but reasonably accurate for most people

### 3. **Distance Calculation (Simple Math)**
```
steps_counted × stride_length = distance_walked
```

**Example:**
- 1000 steps × 0.75m = 750 meters = 0.75 km

---

## GPS vs Stride-Based Comparison

| Method | How It Knows Distance | Pros | Cons |
|--------|----------------------|------|------|
| **GPS** | Tracks your physical location coordinates over time, calculates distance between points | Very accurate outdoors, shows exact path | Battery drain, doesn't work indoors, needs location permissions, complex |
| **Stride-Based** | Counts steps with accelerometer, multiplies by estimated stride length | Works anywhere (indoor/outdoor), battery efficient, simple, no permissions | Less accurate, estimate-based, can't show path on map |

---

## Why Stride-Based for StepEeeasy?

1. **Works indoors** - treadmills, shopping malls, etc.
2. **Battery efficient** - step sensor uses very little power
3. **Privacy-friendly** - no location tracking
4. **Simpler** - easier to implement and maintain
5. **Good enough** - for general fitness tracking, ±10% accuracy is acceptable

---

## Real-World Accuracy

Stride-based is typically **85-95% accurate** compared to GPS for:
- Regular walking
- Consistent terrain
- Average height people

It's less accurate for:
- Running (different stride)
- Hiking (irregular terrain)
- Very tall/short people (formula is approximate)

---

## Implementation in StepEeeasy

### Step 1: Read Step Count
```kotlin
// Android's built-in step counter sensor
val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
// Sensor returns total steps since device boot
// We track baseline when walk starts, then calculate delta
```

### Step 2: Get User Height
```kotlin
// From Settings screen (DataStore)
val heightCm = settingsRepository.userHeightCm.first()
```

### Step 3: Calculate Distance
```kotlin
fun calculateDistance(steps: Int, heightCm: Int): Double {
    val strideLengthMeters = (heightCm * 0.43) / 100.0
    val distanceMeters = steps * strideLengthMeters
    return distanceMeters / 1000.0  // Convert to kilometers
}
```

### Step 4: Display to User
```kotlin
// Example: 5000 steps, height 175cm
// stride = 175 * 0.43 / 100 = 0.7525m
// distance = 5000 * 0.7525 = 3762.5m = 3.76 km
```

---

## Bottom Line

The device uses its **accelerometer to physically count steps**, then uses your height to estimate how far each step takes you. No GPS needed! 🚶‍♂️📱

**Key takeaway:** Stride-based tracking is a practical, battery-efficient alternative to GPS for fitness apps where approximate distance is acceptable.
