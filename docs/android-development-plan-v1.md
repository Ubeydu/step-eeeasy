# StepEasy - Android/Kotlin Development Plan (Production-Ready)

**Project Type:** High School Final Project
**Target Platform:** Android (API 28+)
**Architecture:** Clean Architecture + Repository Pattern
**UI Framework:** XML Layouts + Material Design 3
**Database:** Room ORM with SQLite
**State Management:** ViewModel + LiveData
**Priority:** Home → Settings → History → Paths (Decoupled)

---

## 1. Project Structure & Architecture Overview

```
app/
├── src/main/
│   ├── kotlin/com/stepeeasy/
│   │   ├── di/                          # Dependency Injection (Hilt)
│   │   │   └── AppModule.kt
│   │   │
│   │   ├── data/                        # Data Layer
│   │   │   ├── local/
│   │   │   │   ├── database/
│   │   │   │   │   ├── WalkDao.kt
│   │   │   │   │   ├── WalkDatabase.kt
│   │   │   │   │   └── AppDatabase.kt
│   │   │   │   ├── datastore/
│   │   │   │   │   └── SettingsDataStore.kt
│   │   │   │   └── repository/
│   │   │   │       ├── WalkRepository.kt
│   │   │   │       └── SettingsRepository.kt
│   │   │   └── model/
│   │   │       ├── WalkEntity.kt
│   │   │       └── GpsPointEntity.kt
│   │   │
│   │   ├── domain/                      # Domain Layer (Business Logic)
│   │   │   ├── model/
│   │   │   │   ├── Walk.kt
│   │   │   │   ├── GpsPoint.kt
│   │   │   │   └── DailyStats.kt
│   │   │   ├── repository/
│   │   │   │   ├── IWalkRepository.kt
│   │   │   │   └── ISettingsRepository.kt
│   │   │   └── usecase/
│   │   │       ├── StartWalkUseCase.kt
│   │   │       ├── StopWalkUseCase.kt
│   │   │       ├── GetDailyStatsUseCase.kt
│   │   │       └── GetAllWalksUseCase.kt
│   │   │
│   │   ├── presentation/                # Presentation Layer (UI)
│   │   │   ├── home/
│   │   │   │   ├── HomeActivity.kt
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   └── HomeFragment.kt
│   │   │   ├── history/
│   │   │   │   ├── HistoryFragment.kt
│   │   │   │   ├── HistoryViewModel.kt
│   │   │   │   └── HistoryChartAdapter.kt
│   │   │   ├── paths/
│   │   │   │   ├── PathsFragment.kt
│   │   │   │   ├── PathsViewModel.kt
│   │   │   │   └── PathsAdapter.kt
│   │   │   ├── settings/
│   │   │   │   ├── SettingsFragment.kt
│   │   │   │   └── SettingsViewModel.kt
│   │   │   └── navigation/
│   │   │       └── NavigationManager.kt
│   │   │
│   │   ├── service/                     # Background Services
│   │   │   ├── WalkTrackingService.kt
│   │   │   ├── SensorManager.kt
│   │   │   └── GpsLocationManager.kt
│   │   │
│   │   ├── util/                        # Utilities
│   │   │   ├── DistanceCalculator.kt
│   │   │   ├── StrideCalculator.kt
│   │   │   ├── DateFormatter.kt
│   │   │   └── Constants.kt
│   │   │
│   │   └── MainActivity.kt
│   │
│   └── res/
│       ├── layout/                      # XML Layouts
│       │   ├── activity_main.xml
│       │   ├── fragment_home.xml
│       │   ├── fragment_history.xml
│       │   ├── fragment_paths.xml
│       │   ├── fragment_settings.xml
│       │   ├── item_walk_card.xml
│       │   └── item_history_day.xml
│       ├── values/
│       │   ├── colors.xml               # Material Design 3 Colors
│       │   ├── themes.xml
│       │   ├── dimens.xml
│       │   └── strings.xml
│       └── drawable/
│           └── [icons & drawables]
│
└── build.gradle.kts                     # Dependencies
```

---

## 2. Core Dependencies

### `build.gradle.kts` (App Module)

```kotlin
dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // Material Design 3
    implementation("com.google.android.material:material:1.11.0")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore (Settings)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Dependency Injection (Hilt)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Coroutines (Async/Background Tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")

    // Maps (Leaflet via WebView or Google Maps)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Charts (for History screen)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Serialization (JSON)
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

## 3. Database Schema (Room ORM)

### `data/local/database/WalkEntity.kt`

```kotlin
@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Long,  // Unix timestamp in milliseconds

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "total_steps")
    val totalSteps: Int,

    @ColumnInfo(name = "distance_meters")
    val distanceMeters: Double,


    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,

    @ColumnInfo(name = "date")
    val date: String  // Format: "YYYY-MM-DD" for easy grouping
)
```

### `data/local/database/GpsPointEntity.kt`

```kotlin
@Entity(
    tableName = "gps_points",
    foreignKeys = [
        ForeignKey(
            entity = WalkEntity::class,
            parentColumns = ["id"],
            childColumns = ["walk_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "walk_id")
    val walkId: Long,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "altitude")

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,  // Unix timestamp in milliseconds

    @ColumnInfo(name = "accuracy")
    val accuracy: Float = 0f
)
```

### `data/local/database/AppDatabase.kt`

```kotlin
@Database(
    entities = [WalkEntity::class, GpsPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
    abstract fun gpsPointDao(): GpsPointDao
}
```

### `data/local/database/WalkDao.kt`

```kotlin
@Dao
interface WalkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalk(walk: WalkEntity): Long

    @Update
    suspend fun updateWalk(walk: WalkEntity)

    @Query("SELECT * FROM walks WHERE id = :walkId")
    fun getWalkById(walkId: Long): Flow<WalkEntity?>

    @Query("SELECT * FROM walks ORDER BY start_time DESC")
    fun getAllWalks(): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE date = :date")
    fun getWalksByDate(date: String): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE date BETWEEN :startDate AND :endDate ORDER BY start_time DESC")
    fun getWalksByDateRange(startDate: String, endDate: String): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE is_active = 1 LIMIT 1")
    fun getActiveWalk(): Flow<WalkEntity?>

    @Query("DELETE FROM walks")
    suspend fun deleteAllWalks()

    @Query("SELECT SUM(total_steps) as total_steps, SUM(distance_meters) as total_distance FROM walks WHERE date = :date")
    fun getDailyStats(date: String): Flow<DailyStatsEntity?>
}
```

### `data/local/database/GpsPointDao.kt`

```kotlin
@Dao
interface GpsPointDao {

    @Insert
    suspend fun insertGpsPoint(point: GpsPointEntity)

    @Insert
    suspend fun insertGpsPoints(points: List<GpsPointEntity>)

    @Query("SELECT * FROM gps_points WHERE walk_id = :walkId ORDER BY timestamp ASC")
    fun getGpsPointsByWalk(walkId: Long): Flow<List<GpsPointEntity>>

    @Query("DELETE FROM gps_points WHERE walk_id = :walkId")
    suspend fun deleteGpsPointsByWalk(walkId: Long)
}
```

---

## 4. Domain Layer (Business Logic)

### `domain/model/Walk.kt`

```kotlin
data class Walk(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val totalSteps: Int,
    val distanceMeters: Double,
    val isActive: Boolean = false,
    val gpsPoints: List<GpsPoint> = emptyList(),
    val date: LocalDate
) {
    val durationSeconds: Long
        get() = if (endTime != null) {
            Duration.between(startTime, endTime).seconds
        } else {
            0L
        }

    val distanceKm: Double
        get() = distanceMeters / 1000.0
}
```

### `domain/model/GpsPoint.kt`

```kotlin
data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime,
    val accuracy: Float = 0f
)
```

### `domain/model/DailyStats.kt`

```kotlin
data class DailyStats(
    val date: LocalDate,
    val totalSteps: Int,
    val totalDistanceMeters: Double,
    val walkCount: Int = 0
) {
    val totalDistanceKm: Double
        get() = totalDistanceMeters / 1000.0
}
```

### `domain/repository/IWalkRepository.kt`

```kotlin
interface IWalkRepository {

    suspend fun startWalk(): Walk

    suspend fun stopWalk(walkId: Long): Walk

    suspend fun pauseWalk(walkId: Long): Walk

    suspend fun resumeWalk(walkId: Long): Walk

    fun getActiveWalk(): Flow<Walk?>

    fun getAllWalks(): Flow<List<Walk>>

    fun getWalksByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Walk>>

    fun getDailyStats(date: LocalDate): Flow<DailyStats>

    suspend fun addGpsPoint(walkId: Long, gpsPoint: GpsPoint)


    suspend fun deleteAllWalks()
}
```

### `domain/usecase/StartWalkUseCase.kt`

```kotlin
class StartWalkUseCase @Inject constructor(
    private val walkRepository: IWalkRepository
) {
    suspend operator fun invoke(): Walk = walkRepository.startWalk()
}
```

---

## 5. Data Layer (Repository Implementation)

### `data/repository/WalkRepository.kt`

```kotlin
class WalkRepository @Inject constructor(
    private val walkDao: WalkDao,
    private val gpsPointDao: GpsPointDao,
    private val settingsRepository: ISettingsRepository,
    private val distanceCalculator: DistanceCalculator,
    private val strideCalculator: StrideCalculator
) : IWalkRepository {

    private val _currentWalkId = MutableStateFlow<Long?>(null)

    override suspend fun startWalk(): Walk {
        val now = LocalDateTime.now()
        val date = now.toLocalDate().toString()

        val walkEntity = WalkEntity(
            startTime = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTime = null,
            totalSteps = 0,
            distanceMeters = 0.0,
            isActive = true,
            date = date
        )

        val walkId = walkDao.insertWalk(walkEntity)
        _currentWalkId.value = walkId

        return walkEntity.toDomain(emptyList(), walkId)
    }

    override suspend fun stopWalk(walkId: Long): Walk {
        val walk = walkDao.getWalkById(walkId).first()
            ?: throw IllegalArgumentException("Walk not found")

        val updatedWalk = walk.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )

        walkDao.updateWalk(updatedWalk)
        _currentWalkId.value = null

        val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkId).first()
        return updatedWalk.toDomain(gpsPoints, walkId)
    }

    override fun getActiveWalk(): Flow<Walk?> {
        return walkDao.getActiveWalk().mapLatest { walkEntity ->
            if (walkEntity != null) {
                val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkEntity.id).first()
                walkEntity.toDomain(gpsPoints, walkEntity.id)
            } else {
                null
            }
        }
    }

    override fun getAllWalks(): Flow<List<Walk>> {
        return walkDao.getAllWalks().mapLatest { walks ->
            walks.map { walkEntity ->
                val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkEntity.id).first()
                walkEntity.toDomain(gpsPoints, walkEntity.id)
            }
        }
    }

    override suspend fun addGpsPoint(walkId: Long, gpsPoint: GpsPoint) {
        val gpsPointEntity = GpsPointEntity(
            walkId = walkId,
            latitude = gpsPoint.latitude,
            longitude = gpsPoint.longitude,
            altitude = gpsPoint.altitude,
            timestamp = gpsPoint.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            accuracy = gpsPoint.accuracy
        )

        gpsPointDao.insertGpsPoint(gpsPointEntity)

        val allPoints = gpsPointDao.getGpsPointsByWalk(walkId).first()

        val walk = walkDao.getWalkById(walkId).first()
            ?: throw IllegalArgumentException("Walk not found")

        val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkId).first()
        val distance = distanceCalculator.calculateDistance(
            gpsPoints.map { LatLng(it.latitude, it.longitude) }
        )

        val updatedWalk = walk.copy(
            totalSteps = steps,
            distanceMeters = distance,
        )

        walkDao.updateWalk(updatedWalk)
    }

    override suspend fun deleteAllWalks() {
        walkDao.deleteAllWalks()
    }

    override fun getWalksByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Walk>> {
        // Implement based on your needs
        return emptyFlow()
    }

    override fun getDailyStats(date: LocalDate): Flow<DailyStats> {
        // Implement aggregation logic
        return emptyFlow()
    }

    override suspend fun pauseWalk(walkId: Long): Walk {
        // Not immediately implemented—advanced feature
        TODO("Implement pause functionality")
    }

    override suspend fun resumeWalk(walkId: Long): Walk {
        // Not immediately implemented—advanced feature
        TODO("Implement resume functionality")
    }

        var gain = 0.0
        for (i in 1 until altitudes.size) {
            val diff = altitudes[i] - altitudes[i - 1]
            if (diff > 0) gain += diff
        }
        return gain
    }
}
```

---

## 6. Sensor Integration

### `service/SensorManager.kt`

```kotlin
class SensorManager @Inject constructor(
    private val context: Context
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR)

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> = _stepCount

    private var stepListener: android.hardware.SensorEventListener? = null
    private var initialStepCount: Int = 0

    fun startStepCounting(callback: (Int) -> Unit) {
        if (stepCounterSensor == null) {
            Log.w("SensorManager", "Step Counter sensor not available")
            return
        }

        stepListener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                if (event.sensor.type == android.hardware.Sensor.TYPE_STEP_COUNTER) {
                    if (initialStepCount == 0) {
                        initialStepCount = event.values[0].toInt()
                    }

                    val currentSteps = (event.values[0].toInt() - initialStepCount).coerceAtLeast(0)
                    _stepCount.postValue(currentSteps)
                    callback(currentSteps)
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(
            stepListener,
            stepCounterSensor,
            android.hardware.SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stopStepCounting() {
        stepListener?.let {
            sensorManager.unregisterListener(it)
        }
        initialStepCount = 0
    }
}
```

---

## 7. GPS Location Tracking

### `service/GpsLocationManager.kt`

```kotlin
class GpsLocationManager @Inject constructor(
    private val context: Context,
    private val walkRepository: IWalkRepository
) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _gpsUpdates = MutableLiveData<GpsPoint>()
    val gpsUpdates: LiveData<GpsPoint> = _gpsUpdates

    private val locationRequest = LocationRequest.create().apply {
        interval = 5000L  // 5 seconds
        fastestInterval = 2000L
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var locationCallback: LocationCallback? = null

    fun startLocationUpdates(walkId: Long) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val gpsPoint = GpsPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        timestamp = LocalDateTime.now(),
                        accuracy = location.accuracy
                    )

                    _gpsUpdates.postValue(gpsPoint)

                    // Persist to database
                    CoroutineScope(Dispatchers.IO).launch {
                        walkRepository.addGpsPoint(walkId, gpsPoint)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
```

---

## 8. Foreground Service for Background Tracking

### `service/WalkTrackingService.kt`

```kotlin
@HiltAndroidApp
class WalkTrackingService : Service() {

    @Inject
    lateinit var walkRepository: IWalkRepository

    @Inject
    lateinit var sensorManager: SensorManager

    @Inject
    lateinit var gpsLocationManager: GpsLocationManager

    private var currentWalkId: Long? = null
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "walk_tracking"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_WALK -> startWalkTracking()
            ACTION_STOP_WALK -> stopWalkTracking()
        }

        return START_STICKY
    }

    private fun startWalkTracking() {
        CoroutineScope(Dispatchers.IO).launch {
            val walk = walkRepository.startWalk()
            currentWalkId = walk.id

            sensorManager.startStepCounting { steps ->
                updateNotification(steps)
            }

            gpsLocationManager.startLocationUpdates(walk.id)

            startForeground(NOTIFICATION_ID, buildNotification("Walk in progress..."))
        }
    }

    private fun stopWalkTracking() {
        currentWalkId?.let { walkId ->
            CoroutineScope(Dispatchers.IO).launch {
                walkRepository.stopWalk(walkId)
            }
        }

        sensorManager.stopStepCounting()
        gpsLocationManager.stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StepEasy")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_step)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = buildNotification("Steps: $steps")
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Walk Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val ACTION_START_WALK = "ACTION_START_WALK"
        const val ACTION_STOP_WALK = "ACTION_STOP_WALK"
    }
}
```

---

## 9. ViewModels (Presentation Layer)

### `presentation/home/HomeViewModel.kt`

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startWalkUseCase: StartWalkUseCase,
    private val stopWalkUseCase: StopWalkUseCase,
    private val walkRepository: IWalkRepository
) : ViewModel() {

    private val _currentWalk = MutableLiveData<Walk?>()
    val currentWalk: LiveData<Walk?> = _currentWalk

    private val _timerSeconds = MutableLiveData<Long>(0L)
    val timerSeconds: LiveData<Long> = _timerSeconds

    private var timerJob: Job? = null

    init {
        loadActiveWalk()
    }

    private fun loadActiveWalk() {
        viewModelScope.launch {
            walkRepository.getActiveWalk().collect { walk ->
                _currentWalk.value = walk
                if (walk != null) startTimer()
            }
        }
    }

    fun onStartClicked() {
        viewModelScope.launch {
            try {
                val walk = startWalkUseCase()
                _currentWalk.value = walk
                startTimer()
                // Start foreground service
                val intent = Intent(context, WalkTrackingService::class.java).apply {
                    action = WalkTrackingService.ACTION_START_WALK
                }
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to start walk", e)
            }
        }
    }

    fun onStopClicked() {
        viewModelScope.launch {
            try {
                _currentWalk.value?.let { walk ->
                    stopWalkUseCase(walk.id)
                    stopTimer()
                }
                // Stop foreground service
                val intent = Intent(context, WalkTrackingService::class.java).apply {
                    action = WalkTrackingService.ACTION_STOP_WALK
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to stop walk", e)
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _timerSeconds.value = (_timerSeconds.value ?: 0) + 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _timerSeconds.value = 0
    }
}
```

---

## 10. Dependency Injection Setup (Hilt)

### `di/AppModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "stepeeasy.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Singleton
    @Provides
    fun provideWalkDao(database: AppDatabase): WalkDao = database.walkDao()

    @Singleton
    @Provides
    fun provideGpsPointDao(database: AppDatabase): GpsPointDao = database.gpsPointDao()

    @Singleton
    @Provides
    fun provideWalkRepository(
        walkDao: WalkDao,
        gpsPointDao: GpsPointDao,
        settingsRepository: ISettingsRepository,
        distanceCalculator: DistanceCalculator,
        strideCalculator: StrideCalculator
    ): IWalkRepository = WalkRepository(
        walkDao, gpsPointDao, settingsRepository,
        distanceCalculator, strideCalculator
    )

    @Singleton
    @Provides
    fun provideDistanceCalculator(): DistanceCalculator = DistanceCalculator()

    @Singleton
    @Provides
    fun provideStrideCalculator(): StrideCalculator = StrideCalculator()
}
```

---

## 11. Utility Classes

### `util/DistanceCalculator.kt`

```kotlin
class DistanceCalculator {

    /**
     * Calculate total distance from GPS points using Haversine formula
     */
    fun calculateDistance(points: List<LatLng>): Double {
        var totalDistance = 0.0

        for (i in 1 until points.size) {
            totalDistance += haversineDistance(points[i - 1], points[i])
        }

        return totalDistance
    }

    private fun haversineDistance(point1: LatLng, point2: LatLng): Double {
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)

        val c = 2 * Math.asin(Math.sqrt(a))

        return EARTH_RADIUS_KM * c * 1000  // Convert to meters
    }

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
    }
}
```

### `util/StrideCalculator.kt`

```kotlin
class StrideCalculator {

    /**
     * Estimate stride length from user height
     * Formula: Stride (cm) = Height (cm) × 0.43
     */
    fun calculateStrideFromHeight(heightCm: Int): Double {
        return heightCm * 0.43
    }

    /**
     * Calculate distance from steps and stride length
     */
    fun calculateDistance(steps: Int, strideCm: Double): Double {
        return (steps * strideCm) / 100.0  // Convert to meters
    }
}
```

### `util/DateFormatter.kt`

```kotlin
object DateFormatter {

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun formatDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return localDate.format(formatter)
    }

    fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfWeek = date.with(DayOfWeek.SUNDAY)
        val endOfWeek = date.with(DayOfWeek.SATURDAY)
        return startOfWeek to endOfWeek
    }
}
```

---

## 12. AndroidManifest.xml Permissions

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />

<application>
    <!-- Activities & Services -->
    <activity android:name=".MainActivity" />
    <service
        android:name=".service.WalkTrackingService"
        android:foregroundServiceType="location"
        android:exported="false" />
</application>
```

---

## 13. Implementation Order (Phase-Based)

### **Phase 1: Foundation & Home Screen (Week 1)**
1. Set up Hilt dependency injection
2. Create Room database with WalkEntity and GpsPointEntity
3. Implement WalkRepository (data layer)
4. Create StartWalkUseCase and StopWalkUseCase
5. Build HomeViewModel
6. Design XML layouts for Home screen (activity_main.xml + fragment_home.xml)
7. Implement HomeFragment with basic UI

**Deliverable:** Users can tap "START", see a live timer, and tap "STOP". No sensor integration yet.

---

### **Phase 2: Settings Screen (Week 1-2)**
1. Create SettingsRepository with DataStore
2. Build SettingsFragment and SettingsViewModel
3. Add height input field
4. Implement Activity Recognition toggle
5. Add "Clear Recorded Walks" button
6. Display footer information

**Deliverable:** Settings are persistent and affect calculations. User height is now available for stride estimation.

---

### **Phase 3: Sensor Integration (Week 2)**
1. Implement SensorManager for step counting
2. Integrate GpsLocationManager for location tracking
3. Create WalkTrackingService for background tracking
4. Request runtime permissions (Location + Activity Recognition)
5. Update HomeViewModel to display real steps and distance (using actual user height from Settings)
6. Test on actual Android device

**Deliverable:** Live step counter and distance calculation during walks, with stride length based on user's configured height.

---

### **Phase 4: History Screen (Week 3)**
1. Implement getDailyStats usecase
2. Create data aggregation logic (group walks by day)
3. Integrate MPAndroidChart library for bar chart
4. Build HistoryFragment and HistoryViewModel
5. Add week navigation arrows
6. Implement tap-to-show tooltip

**Deliverable:** Users can browse historical data with interactive charts.

---

### **Phase 5: Paths Screen (Week 4)**
1. Fetch GPS points for each walk
2. Integrate Google Maps or Leaflet for rendering path polylines
3. Create PathsAdapter for list rendering
4. Build PathsFragment and PathsViewModel
5. Add lazy loading for map thumbnails

**Deliverable:** Visual representation of all recorded walks with GPS paths.

---

## 14. Key Technical Decisions

| Decision | Rationale |
|----------|-----------|
| **Room ORM** | Strongly typed, compile-time safe, standard Android practice. Superior to SQLite raw queries. |
| **Flow/LiveData** | Reactive data binding ensures UI always reflects latest state. Handles configuration changes gracefully. |
| **Hilt DI** | Reduces boilerplate, enables easy testing, standard Google-recommended approach. |
| **Foreground Service** | Guarantees background step counting even if app is killed. Simpler than JobScheduler for continuous tracking. |
| **Built-in Step Counter** | More accurate and battery-efficient than custom accelerometer detection. Only requires low power. |
| **GPS for Paths** | More reliable than dead reckoning from steps. Enables visual map representation. |
| **DataStore over SharedPreferences** | Newer standard, type-safe, coroutines-based. Better for settings persistence. |
| **XML Layouts** | Simpler for a team project, easier to debug UI layout issues. Familiar to developers coming from web. |

---

## 15. Testing Strategy

### Unit Tests
```kotlin
// Example: DistanceCalculatorTest
class DistanceCalculatorTest {
    private lateinit var calculator: DistanceCalculator

    @Before
    fun setup() {
        calculator = DistanceCalculator()
    }

    @Test
    fun testHaversineDistance() {
        val point1 = LatLng(40.7128, -74.0060)  // NYC
        val point2 = LatLng(40.7580, -73.9855)  // Manhattan
        val distance = calculator.calculateDistance(listOf(point1, point2))
        assertTrue(distance in 5000.0..6000.0)  // ~5.5 km
    }
}
```

### Instrumented Tests
```kotlin
// Example: WalkRepositoryTest
@RunWith(AndroidJUnit4::class)
class WalkRepositoryTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testStartWalkInsertsRecord() = runBlocking {
        // Given
        val repo = WalkRepository(...)

        // When
        val walk = repo.startWalk()

        // Then
        assertNotNull(walk)
        assertTrue(walk.isActive)
    }
}
```

---

## 16. Performance & Optimization

1. **Lazy Loading Maps:** Only initialize map views when visible
2. **GPS Batch Inserts:** Insert 10+ GPS points in one DB transaction
3. **Flow Collection:** Use `.collect { }` instead of repeated `.first()` calls
4. **Coroutine Scope:** Always cancel scopes in onDestroy to prevent leaks
5. **Step Counter:** Update UI every second, not every sensor event
6. **Database Indexing:** Add index on `WalkEntity.date` for faster queries

---

## 17. Future Enhancements (Post-MVP)

- [ ] Pause/Resume walk sessions
- [ ] Walk route editing (manually adjust GPS path)
- [ ] Social sharing (export walk stats as image)
- [ ] Calorie estimation based on stride and speed
- [ ] Dark/Light theme toggle
- [ ] Watch sync via Wear OS
- [ ] Cloud backup (Firebase/Dropbox)
- [ ] Achievements/badges system

---

## Conclusion

This architecture provides:
- **Scalability**: Easy to add new features without modifying existing code
- **Testability**: Clear separation of concerns makes unit testing straightforward
- **Maintainability**: Decoupled modules mean Home screen works independently
- **Robustness**: Proper error handling, null safety with Kotlin

Follow this plan phase-by-phase, and you'll have a production-quality Android app suitable for a high school final project.

Good luck! 🚀
