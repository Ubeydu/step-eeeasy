# StepEeeasy - Technical Reference (Jetpack Compose)

This document contains all code examples, configurations, and implementation details for StepEeeasy. Each section is tagged with the phase where it's used.

---

## Quick Navigation Index

- [Configuration](#configuration)
  - [Dependencies](#dependencies)
  - [Permissions & Manifest](#permissions--manifest)
- [Data Layer](#data-layer)
  - [Database (Room)](#database-room)
  - [DataStore (Settings)](#datastore-settings)
  - [Repositories](#repositories)
- [Domain Layer](#domain-layer)
  - [Models](#models)
  - [Repository Interfaces](#repository-interfaces)
  - [Use Cases](#use-cases)
- [Presentation Layer](#presentation-layer)
  - [Home Screen](#home-screen)
  - [Settings Screen](#settings-screen)
  - [History Screen](#history-screen)
  - [Paths Screen](#paths-screen)
  - [Navigation](#navigation)
  - [Theme](#theme)
- [Service Layer](#service-layer)
  - [WalkTrackingService](#walktrackingservice)
  - [SensorManager](#sensormanager)
  - [GpsLocationManager](#gpslocationmanager)
- [Dependency Injection](#dependency-injection)
- [Utilities](#utilities)
- [Testing](#testing)
- [Common Patterns](#common-patterns)

---

## Configuration

### Dependencies

**Used in**: All Phases
**File**: `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.stepeeeasy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.stepeeeasy"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // ViewModel & Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore (Settings)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Dependency Injection (Hilt)
    val hiltVersion = "2.52"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:maps-compose:6.2.1")

    // Charts (MPAndroidChart)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

**Project level `build.gradle.kts` additions:**

```kotlin
plugins {
    // ... existing plugins
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
```

**settings.gradle.kts additions (for MPAndroidChart):**

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add this line
    }
}
```

---

### Permissions & Manifest

**Used in**: Phase 3
**File**: `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Location Permissions -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Activity Recognition (Step Counter) -->
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

    <!-- Foreground Service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

    <!-- Notifications (API 33+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Internet (for Maps) -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Declare hardware features as optional (for compatibility) -->
    <uses-feature
        android:name="android.hardware.sensor.stepcounter"
        android:required="false" />

    <application
        android:name=".StepEeeasyApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.StepEeeasy"
        tools:targetApi="31">

        <!-- Google Maps API Key -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_MAPS_API_KEY_HERE" />

        <!-- Main Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.StepEeeasy">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Walk Tracking Foreground Service -->
        <service
            android:name=".service.WalkTrackingService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="location" />
    </application>

</manifest>
```

---

## Data Layer

### Database (Room)

#### WalkEntity

**Used in**: Phase 1, Task 2
**File**: `data/local/database/WalkEntity.kt`

```kotlin
package com.example.stepeeeasy.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Long,  // Unix timestamp in milliseconds

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "total_steps")
    val totalSteps: Int = 0,

    @ColumnInfo(name = "distance_meters")
    val distanceMeters: Double = 0.0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,

    @ColumnInfo(name = "date")
    val date: String  // Format: "YYYY-MM-DD" for easy grouping
)
```

---

#### GpsPointEntity

**Used in**: Phase 1, Task 2
**File**: `data/local/database/GpsPointEntity.kt`

```kotlin
package com.example.stepeeeasy.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_points",
    foreignKeys = [
        ForeignKey(
            entity = WalkEntity::class,
            parentColumns = ["id"],
            childColumns = ["walk_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["walk_id"])]
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

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,  // Unix timestamp in milliseconds

    @ColumnInfo(name = "accuracy")
    val accuracy: Float = 0f
)
```

---

#### WalkDao

**Used in**: Phase 1, Task 2; Phase 4 (additional queries)
**File**: `data/local/database/WalkDao.kt`

```kotlin
package com.example.stepeeeasy.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM walks WHERE date = :date ORDER BY start_time DESC")
    fun getWalksByDate(date: String): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE date BETWEEN :startDate AND :endDate ORDER BY start_time DESC")
    fun getWalksByDateRange(startDate: String, endDate: String): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE is_active = 1 LIMIT 1")
    fun getActiveWalk(): Flow<WalkEntity?>

    @Query("DELETE FROM walks")
    suspend fun deleteAllWalks()

    @Query("DELETE FROM walks WHERE id = :walkId")
    suspend fun deleteWalkById(walkId: Long)
}
```

---

#### GpsPointDao

**Used in**: Phase 1, Task 2
**File**: `data/local/database/GpsPointDao.kt`

```kotlin
package com.example.stepeeeasy.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

#### AppDatabase

**Used in**: Phase 1, Task 2
**File**: `data/local/database/AppDatabase.kt`

```kotlin
package com.example.stepeeeasy.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

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

---

### DataStore (Settings)

**Used in**: Phase 2, Task 1
**File**: `data/local/datastore/SettingsDataStore.kt`

```kotlin
package com.example.stepeeeasy.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val USER_HEIGHT = intPreferencesKey("user_height")
        val ACTIVITY_RECOGNITION_ENABLED = booleanPreferencesKey("activity_recognition_enabled")
    }

    val userHeight: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_HEIGHT] ?: 170 // Default: 170 cm
        }

    val activityRecognitionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVITY_RECOGNITION_ENABLED] ?: false
        }

    suspend fun setUserHeight(height: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_HEIGHT] = height
        }
    }

    suspend fun setActivityRecognitionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVITY_RECOGNITION_ENABLED] = enabled
        }
    }
}
```

---

### Repositories

#### WalkRepository

**Used in**: Phase 1, Task 3; Phase 3 (updates with sensors)
**File**: `data/repository/WalkRepositoryImpl.kt`

```kotlin
package com.example.stepeeeasy.data.repository

import com.example.stepeeeasy.data.local.database.GpsPointDao
import com.example.stepeeeasy.data.local.database.GpsPointEntity
import com.example.stepeeeasy.data.local.database.WalkDao
import com.example.stepeeeasy.data.local.database.WalkEntity
import com.example.stepeeeasy.domain.model.DailyStats
import com.example.stepeeeasy.domain.model.GpsPoint
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.IWalkRepository
import com.example.stepeeeasy.util.DistanceCalculator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WalkRepositoryImpl @Inject constructor(
    private val walkDao: WalkDao,
    private val gpsPointDao: GpsPointDao,
    private val distanceCalculator: DistanceCalculator
) : IWalkRepository {

    override suspend fun startWalk(): Walk {
        val now = System.currentTimeMillis()
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val walkEntity = WalkEntity(
            startTime = now,
            endTime = null,
            totalSteps = 0,
            distanceMeters = 0.0,
            isActive = true,
            date = date
        )

        val walkId = walkDao.insertWalk(walkEntity)

        return Walk(
            id = walkId,
            startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault()),
            endTime = null,
            totalSteps = 0,
            distanceMeters = 0.0,
            isActive = true,
            gpsPoints = emptyList(),
            date = LocalDate.now()
        )
    }

    override suspend fun stopWalk(walkId: Long): Walk {
        val walkEntity = walkDao.getWalkById(walkId).first()
            ?: throw IllegalArgumentException("Walk not found: $walkId")

        val updatedWalk = walkEntity.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )

        walkDao.updateWalk(updatedWalk)

        val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkId).first()
        return updatedWalk.toDomainModel(gpsPoints)
    }

    override suspend fun updateWalkMetrics(walkId: Long, steps: Int, distance: Double) {
        val walkEntity = walkDao.getWalkById(walkId).first()
            ?: throw IllegalArgumentException("Walk not found: $walkId")

        val updatedWalk = walkEntity.copy(
            totalSteps = steps,
            distanceMeters = distance
        )

        walkDao.updateWalk(updatedWalk)
    }

    override fun getActiveWalk(): Flow<Walk?> {
        return walkDao.getActiveWalk().map { walkEntity ->
            walkEntity?.let {
                val gpsPoints = gpsPointDao.getGpsPointsByWalk(it.id).first()
                it.toDomainModel(gpsPoints)
            }
        }
    }

    override fun getAllWalks(): Flow<List<Walk>> {
        return walkDao.getAllWalks().map { walks ->
            walks.map { walkEntity ->
                val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkEntity.id).first()
                walkEntity.toDomainModel(gpsPoints)
            }
        }
    }

    override fun getWalksByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Walk>> {
        val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        return walkDao.getWalksByDateRange(startDateStr, endDateStr).map { walks ->
            walks.map { walkEntity ->
                val gpsPoints = gpsPointDao.getGpsPointsByWalk(walkEntity.id).first()
                walkEntity.toDomainModel(gpsPoints)
            }
        }
    }

    override fun getDailyStats(date: LocalDate): Flow<DailyStats?> {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        return walkDao.getWalksByDate(dateStr).map { walks ->
            if (walks.isEmpty()) {
                null
            } else {
                DailyStats(
                    date = date,
                    totalSteps = walks.sumOf { it.totalSteps },
                    totalDistanceMeters = walks.sumOf { it.distanceMeters },
                    walkCount = walks.size
                )
            }
        }
    }

    override suspend fun addGpsPoint(walkId: Long, gpsPoint: GpsPoint) {
        val gpsPointEntity = GpsPointEntity(
            walkId = walkId,
            latitude = gpsPoint.latitude,
            longitude = gpsPoint.longitude,
            timestamp = gpsPoint.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            accuracy = gpsPoint.accuracy
        )

        gpsPointDao.insertGpsPoint(gpsPointEntity)

        // Update walk distance based on GPS points
        val allPoints = gpsPointDao.getGpsPointsByWalk(walkId).first()
        if (allPoints.size > 1) {
            val latLngPoints = allPoints.map { LatLng(it.latitude, it.longitude) }
            val distance = distanceCalculator.calculateDistance(latLngPoints)

            val walkEntity = walkDao.getWalkById(walkId).first()
            walkEntity?.let {
                walkDao.updateWalk(it.copy(distanceMeters = distance))
            }
        }
    }

    override suspend fun deleteAllWalks() {
        walkDao.deleteAllWalks()
    }

    // Extension function to convert entity to domain model
    private fun WalkEntity.toDomainModel(gpsPoints: List<GpsPointEntity>): Walk {
        return Walk(
            id = this.id,
            startTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this.startTime),
                ZoneId.systemDefault()
            ),
            endTime = this.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            },
            totalSteps = this.totalSteps,
            distanceMeters = this.distanceMeters,
            isActive = this.isActive,
            gpsPoints = gpsPoints.map { it.toDomainModel() },
            date = LocalDate.parse(this.date, DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }

    private fun GpsPointEntity.toDomainModel(): GpsPoint {
        return GpsPoint(
            latitude = this.latitude,
            longitude = this.longitude,
            timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this.timestamp),
                ZoneId.systemDefault()
            ),
            accuracy = this.accuracy
        )
    }
}
```

---

#### SettingsRepository

**Used in**: Phase 2, Task 2
**File**: `data/repository/SettingsRepositoryImpl.kt`

```kotlin
package com.example.stepeeeasy.data.repository

import com.example.stepeeeasy.data.local.datastore.SettingsDataStore
import com.example.stepeeeasy.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ISettingsRepository {

    override fun getUserHeight(): Flow<Int> {
        return settingsDataStore.userHeight
    }

    override suspend fun setUserHeight(heightCm: Int) {
        settingsDataStore.setUserHeight(heightCm)
    }

    override fun isActivityRecognitionEnabled(): Flow<Boolean> {
        return settingsDataStore.activityRecognitionEnabled
    }

    override suspend fun setActivityRecognitionEnabled(enabled: Boolean) {
        settingsDataStore.setActivityRecognitionEnabled(enabled)
    }
}
```

---

## Domain Layer

### Models

#### Walk

**Used in**: Phase 1, Task 3
**File**: `domain/model/Walk.kt`

```kotlin
package com.example.stepeeeasy.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

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
            Duration.between(startTime, LocalDateTime.now()).seconds
        }

    val distanceKm: Double
        get() = distanceMeters / 1000.0

    val durationFormatted: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
}
```

---

#### GpsPoint

**Used in**: Phase 1, Task 3
**File**: `domain/model/GpsPoint.kt`

```kotlin
package com.example.stepeeeasy.domain.model

import java.time.LocalDateTime

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime,
    val accuracy: Float = 0f
)
```

---

#### DailyStats

**Used in**: Phase 4, Task 1
**File**: `domain/model/DailyStats.kt`

```kotlin
package com.example.stepeeeasy.domain.model

import java.time.LocalDate

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

---

### Repository Interfaces

#### IWalkRepository

**Used in**: Phase 1, Task 3
**File**: `domain/repository/IWalkRepository.kt`

```kotlin
package com.example.stepeeeasy.domain.repository

import com.example.stepeeeasy.domain.model.DailyStats
import com.example.stepeeeasy.domain.model.GpsPoint
import com.example.stepeeeasy.domain.model.Walk
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IWalkRepository {

    suspend fun startWalk(): Walk

    suspend fun stopWalk(walkId: Long): Walk

    suspend fun updateWalkMetrics(walkId: Long, steps: Int, distance: Double)

    fun getActiveWalk(): Flow<Walk?>

    fun getAllWalks(): Flow<List<Walk>>

    fun getWalksByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Walk>>

    fun getDailyStats(date: LocalDate): Flow<DailyStats?>

    suspend fun addGpsPoint(walkId: Long, gpsPoint: GpsPoint)

    suspend fun deleteAllWalks()
}
```

---

#### ISettingsRepository

**Used in**: Phase 2, Task 2
**File**: `domain/repository/ISettingsRepository.kt`

```kotlin
package com.example.stepeeeasy.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {

    fun getUserHeight(): Flow<Int>

    suspend fun setUserHeight(heightCm: Int)

    fun isActivityRecognitionEnabled(): Flow<Boolean>

    suspend fun setActivityRecognitionEnabled(enabled: Boolean)
}
```

---

### Use Cases

#### StartWalkUseCase

**Used in**: Phase 1, Task 4
**File**: `domain/usecase/StartWalkUseCase.kt`

```kotlin
package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.IWalkRepository
import javax.inject.Inject

class StartWalkUseCase @Inject constructor(
    private val walkRepository: IWalkRepository
) {
    suspend operator fun invoke(): Walk {
        return walkRepository.startWalk()
    }
}
```

---

#### StopWalkUseCase

**Used in**: Phase 1, Task 4
**File**: `domain/usecase/StopWalkUseCase.kt`

```kotlin
package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.IWalkRepository
import javax.inject.Inject

class StopWalkUseCase @Inject constructor(
    private val walkRepository: IWalkRepository
) {
    suspend operator fun invoke(walkId: Long): Walk {
        return walkRepository.stopWalk(walkId)
    }
}
```

---

#### GetDailyStatsUseCase

**Used in**: Phase 4, Task 2
**File**: `domain/usecase/GetDailyStatsUseCase.kt`

```kotlin
package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.DailyStats
import com.example.stepeeeasy.domain.repository.IWalkRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDailyStatsUseCase @Inject constructor(
    private val walkRepository: IWalkRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailyStats?> {
        return walkRepository.getDailyStats(date)
    }
}
```

---

#### GetAllWalksUseCase

**Used in**: Phase 5, Task 2
**File**: `domain/usecase/GetAllWalksUseCase.kt`

```kotlin
package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.IWalkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllWalksUseCase @Inject constructor(
    private val walkRepository: IWalkRepository
) {
    operator fun invoke(): Flow<List<Walk>> {
        return walkRepository.getAllWalks()
    }
}
```

---

#### ClearAllWalksUseCase

**Used in**: Phase 2, Task 5
**File**: `domain/usecase/ClearAllWalksUseCase.kt`

```kotlin
package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.repository.IWalkRepository
import javax.inject.Inject

class ClearAllWalksUseCase @Inject constructor(
    private val walkRepository: IWalkRepository
) {
    suspend operator fun invoke() {
        walkRepository.deleteAllWalks()
    }
}
```

---

## Presentation Layer

### Home Screen

#### HomeViewModel

**Used in**: Phase 1, Task 5; Phase 3, Task 7 (sensor updates)
**File**: `presentation/home/HomeViewModel.kt`

```kotlin
package com.example.stepeeeasy.presentation.home

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.IWalkRepository
import com.example.stepeeeasy.domain.usecase.StartWalkUseCase
import com.example.stepeeeasy.domain.usecase.StopWalkUseCase
import com.example.stepeeeasy.service.WalkTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentWalk: Walk? = null,
    val isWalkActive: Boolean = false,
    val elapsedSeconds: Long = 0,
    val steps: Int = 0,
    val distanceKm: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startWalkUseCase: StartWalkUseCase,
    private val stopWalkUseCase: StopWalkUseCase,
    private val walkRepository: IWalkRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadActiveWalk()
    }

    private fun loadActiveWalk() {
        viewModelScope.launch {
            walkRepository.getActiveWalk().collect { walk ->
                _uiState.value = _uiState.value.copy(
                    currentWalk = walk,
                    isWalkActive = walk?.isActive ?: false,
                    steps = walk?.totalSteps ?: 0,
                    distanceKm = walk?.distanceKm ?: 0.0
                )

                if (walk?.isActive == true) {
                    startTimer()
                } else {
                    stopTimer()
                }
            }
        }
    }

    fun onStartWalkClicked() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val walk = startWalkUseCase()

                // Start foreground service
                val intent = Intent(context, WalkTrackingService::class.java).apply {
                    action = WalkTrackingService.ACTION_START_WALK
                    putExtra(WalkTrackingService.EXTRA_WALK_ID, walk.id)
                }
                ContextCompat.startForegroundService(context, intent)

                _uiState.value = _uiState.value.copy(
                    currentWalk = walk,
                    isWalkActive = true,
                    isLoading = false
                )

                startTimer()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to start walk: ${e.message}"
                )
            }
        }
    }

    fun onStopWalkClicked() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val currentWalk = _uiState.value.currentWalk
                if (currentWalk != null) {
                    stopWalkUseCase(currentWalk.id)

                    // Stop foreground service
                    val intent = Intent(context, WalkTrackingService::class.java).apply {
                        action = WalkTrackingService.ACTION_STOP_WALK
                    }
                    context.startService(intent)

                    stopTimer()

                    _uiState.value = _uiState.value.copy(
                        currentWalk = null,
                        isWalkActive = false,
                        elapsedSeconds = 0,
                        steps = 0,
                        distanceKm = 0.0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to stop walk: ${e.message}"
                )
            }
        }
    }

    private fun startTimer() {
        stopTimer() // Cancel any existing timer
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // 1 second
                val currentWalk = _uiState.value.currentWalk
                if (currentWalk != null && currentWalk.isActive) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = currentWalk.durationSeconds
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
```

---

#### HomeScreen Composable

**Used in**: Phase 1, Task 6; Phase 3, Task 8 (permission handling)
**File**: `presentation/home/HomeScreen.kt`

```kotlin
package com.example.stepeeeasy.presentation.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var permissionsGranted by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    // Request permissions on first composition
    LaunchedEffect(Unit) {
        val permissionsToRequest = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    HomeScreenContent(
        uiState = uiState,
        permissionsGranted = permissionsGranted,
        onStartClicked = viewModel::onStartWalkClicked,
        onStopClicked = viewModel::onStopWalkClicked,
        onRequestPermissionsClicked = {
            val permissionsToRequest = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.ACTIVITY_RECOGNITION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    permissionsGranted: Boolean,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onRequestPermissionsClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Walk icon
        Icon(
            imageVector = Icons.Default.DirectionsWalk,
            contentDescription = "Walk",
            modifier = Modifier
                .size(120.dp)
                .padding(top = 48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        // Middle section: Metrics
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Timer
            TimerDisplay(
                elapsedSeconds = uiState.elapsedSeconds
            )

            // Steps
            MetricCard(
                label = "Steps",
                value = uiState.steps.toString()
            )

            // Distance
            MetricCard(
                label = "Distance",
                value = String.format("%.2f km", uiState.distanceKm)
            )
        }

        // Bottom section: Start/Stop button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            if (!permissionsGranted) {
                Button(
                    onClick = onRequestPermissionsClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("Grant Permissions", fontSize = 18.sp)
                }
                Text(
                    text = "Location and Activity permissions required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                if (uiState.isWalkActive) {
                    Button(
                        onClick = onStopClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("STOP", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onStartClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("START", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TimerDisplay(elapsedSeconds: Long) {
    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60

    Text(
        text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

### Settings Screen

#### SettingsViewModel

**Used in**: Phase 2, Task 3
**File**: `presentation/settings/SettingsViewModel.kt`

```kotlin
package com.example.stepeeeasy.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.repository.ISettingsRepository
import com.example.stepeeeasy.domain.usecase.ClearAllWalksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userHeight: Int = 170,
    val activityRecognitionEnabled: Boolean = false,
    val showClearDialog: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    private val clearAllWalksUseCase: ClearAllWalksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getUserHeight().collect { height ->
                _uiState.value = _uiState.value.copy(userHeight = height)
            }
        }

        viewModelScope.launch {
            settingsRepository.isActivityRecognitionEnabled().collect { enabled ->
                _uiState.value = _uiState.value.copy(activityRecognitionEnabled = enabled)
            }
        }
    }

    fun onHeightChanged(height: Int) {
        viewModelScope.launch {
            if (height in 50..250) {
                settingsRepository.setUserHeight(height)
                _uiState.value = _uiState.value.copy(userHeight = height)
            }
        }
    }

    fun onActivityRecognitionToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setActivityRecognitionEnabled(enabled)
            _uiState.value = _uiState.value.copy(activityRecognitionEnabled = enabled)
        }
    }

    fun onClearWalksClicked() {
        _uiState.value = _uiState.value.copy(showClearDialog = true)
    }

    fun onClearWalksConfirmed() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, showClearDialog = false)
                clearAllWalksUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "All walks cleared successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to clear walks: ${e.message}"
                )
            }
        }
    }

    fun onClearWalksCancelled() {
        _uiState.value = _uiState.value.copy(showClearDialog = false)
    }

    fun onMessageDismissed() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
```

---

#### SettingsScreen Composable

**Used in**: Phase 2, Task 4
**File**: `presentation/settings/SettingsScreen.kt`

```kotlin
package com.example.stepeeeasy.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SettingsScreenContent(
            uiState = uiState,
            onHeightChanged = viewModel::onHeightChanged,
            onActivityRecognitionToggled = viewModel::onActivityRecognitionToggled,
            onClearWalksClicked = viewModel::onClearWalksClicked,
            onClearWalksConfirmed = viewModel::onClearWalksConfirmed,
            onClearWalksCancelled = viewModel::onClearWalksCancelled,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onHeightChanged: (Int) -> Unit,
    onActivityRecognitionToggled: (Boolean) -> Unit,
    onClearWalksClicked: () -> Unit,
    onClearWalksConfirmed: () -> Unit,
    onClearWalksCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Height input
        HeightInputSection(
            height = uiState.userHeight,
            onHeightChanged = onHeightChanged
        )

        Divider()

        // Activity Recognition toggle
        ActivityRecognitionToggle(
            enabled = uiState.activityRecognitionEnabled,
            onToggled = onActivityRecognitionToggled
        )

        Divider()

        // Clear walks button
        ClearWalksButton(
            onClick = onClearWalksClicked,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        AppInfoFooter()
    }

    // Clear walks confirmation dialog
    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = onClearWalksCancelled,
            title = { Text("Clear All Walks?") },
            text = { Text("This will permanently delete all recorded walks. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = onClearWalksConfirmed,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = onClearWalksCancelled) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HeightInputSection(
    height: Int,
    onHeightChanged: (Int) -> Unit
) {
    var heightText by remember(height) { mutableStateOf(height.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Your Height",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Used to calculate stride length and distance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = heightText,
            onValueChange = { newValue ->
                heightText = newValue
                newValue.toIntOrNull()?.let { heightValue ->
                    if (heightValue in 50..250) {
                        onHeightChanged(heightValue)
                    }
                }
            },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (heightText.toIntOrNull() !in 50..250) {
                    Text("Height must be between 50-250 cm")
                }
            },
            isError = heightText.toIntOrNull() !in 50..250
        )
    }
}

@Composable
private fun ActivityRecognitionToggle(
    enabled: Boolean,
    onToggled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Activity Recognition",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Enable step counting during walks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggled
        )
    }
}

@Composable
private fun ClearWalksButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Recorded Walks")
        }
    }
}

@Composable
private fun AppInfoFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "StepEeeasy v1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "High School Final Project",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

---

### History Screen

#### HistoryViewModel

**Used in**: Phase 4, Task 5
**File**: `presentation/history/HistoryViewModel.kt`

```kotlin
package com.example.stepeeeasy.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.DailyStats
import com.example.stepeeeasy.domain.usecase.GetDailyStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HistoryUiState(
    val weeklyStats: List<DailyStats?> = emptyList(),
    val currentWeekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getDailyStatsUseCase: GetDailyStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyStats()
    }

    fun loadWeeklyStats() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val weekStart = _uiState.value.currentWeekStart
                val weeklyStats = mutableListOf<DailyStats?>()

                // Load stats for 7 days (Sunday to Saturday)
                for (i in 0..6) {
                    val date = weekStart.plusDays(i.toLong())
                    getDailyStatsUseCase(date).collect { stats ->
                        // Add to list (stats can be null if no walks on that day)
                        if (weeklyStats.size == i) {
                            weeklyStats.add(stats)
                        } else {
                            weeklyStats[i] = stats
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    weeklyStats = weeklyStats,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load history: ${e.message}"
                )
            }
        }
    }

    fun onPreviousWeekClicked() {
        _uiState.value = _uiState.value.copy(
            currentWeekStart = _uiState.value.currentWeekStart.minusWeeks(1)
        )
        loadWeeklyStats()
    }

    fun onNextWeekClicked() {
        val nextWeek = _uiState.value.currentWeekStart.plusWeeks(1)
        val today = LocalDate.now()

        // Don't go beyond current week
        if (nextWeek.isBefore(today) || nextWeek.isEqual(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))) {
            _uiState.value = _uiState.value.copy(currentWeekStart = nextWeek)
            loadWeeklyStats()
        }
    }

    fun getWeekRangeText(): String {
        val weekStart = _uiState.value.currentWeekStart
        val weekEnd = weekStart.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        return "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}"
    }
}
```

---

#### HistoryScreen Composable

**Used in**: Phase 4, Task 6
**File**: `presentation/history/HistoryScreen.kt`

```kotlin
package com.example.stepeeeasy.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Week navigation
        WeekNavigationBar(
            weekRangeText = viewModel.getWeekRangeText(),
            onPreviousClicked = viewModel::onPreviousWeekClicked,
            onNextClicked = viewModel::onNextWeekClicked
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chart
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Text(
                text = uiState.error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            WeeklyStepsChart(
                weeklyStats = uiState.weeklyStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics summary
        StatisticsSummary(weeklyStats = uiState.weeklyStats)
    }
}

@Composable
private fun WeekNavigationBar(
    weekRangeText: String,
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week"
            )
        }

        Text(
            text = weekRangeText,
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = onNextClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next week"
            )
        }
    }
}

@Composable
private fun WeeklyStepsChart(
    weeklyStats: List<com.example.stepeeeasy.domain.model.DailyStats?>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setFitBars(true)
                animateY(1000)

                // X-axis setup
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(
                        arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    )
                }

                // Y-axis setup
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(true)
                }
                axisRight.isEnabled = false

                legend.isEnabled = false
            }
        },
        update = { barChart ->
            val entries = weeklyStats.mapIndexed { index, stats ->
                BarEntry(index.toFloat(), stats?.totalSteps?.toFloat() ?: 0f)
            }

            val dataSet = BarDataSet(entries, "Steps").apply {
                color = android.graphics.Color.parseColor("#6750A4") // Material3 primary
                valueTextSize = 10f
            }

            barChart.data = BarData(dataSet)
            barChart.invalidate()
        }
    )
}

@Composable
private fun StatisticsSummary(
    weeklyStats: List<com.example.stepeeeasy.domain.model.DailyStats?>
) {
    val totalSteps = weeklyStats.filterNotNull().sumOf { it.totalSteps }
    val totalDistance = weeklyStats.filterNotNull().sumOf { it.totalDistanceMeters } / 1000.0
    val totalWalks = weeklyStats.filterNotNull().sumOf { it.walkCount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Week Summary",
                style = MaterialTheme.typography.titleMedium
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SummaryRow(label = "Total Steps", value = totalSteps.toString())
            SummaryRow(label = "Total Distance", value = String.format("%.2f km", totalDistance))
            SummaryRow(label = "Total Walks", value = totalWalks.toString())
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
```

---

### Paths Screen

#### PathsViewModel

**Used in**: Phase 5, Task 3
**File**: `presentation/paths/PathsViewModel.kt`

```kotlin
package com.example.stepeeeasy.presentation.paths

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.usecase.GetAllWalksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PathsUiState(
    val walks: List<Walk> = emptyList(),
    val selectedWalkId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PathsViewModel @Inject constructor(
    private val getAllWalksUseCase: GetAllWalksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PathsUiState())
    val uiState: StateFlow<PathsUiState> = _uiState.asStateFlow()

    init {
        loadWalks()
    }

    private fun loadWalks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                getAllWalksUseCase().collect { walks ->
                    _uiState.value = _uiState.value.copy(
                        walks = walks.filter { it.gpsPoints.isNotEmpty() }, // Only show walks with GPS data
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load walks: ${e.message}"
                )
            }
        }
    }

    fun onWalkClicked(walkId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedWalkId = if (_uiState.value.selectedWalkId == walkId) null else walkId
        )
    }
}
```

---

#### PathsScreen Composable

**Used in**: Phase 5, Task 4
**File**: `presentation/paths/PathsScreen.kt`

```kotlin
package com.example.stepeeeasy.presentation.paths

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepeeeasy.domain.model.Walk
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.time.format.DateTimeFormatter

@Composable
fun PathsScreen(
    viewModel: PathsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Paths",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            uiState.walks.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No walks with GPS data yet.\nComplete a walk to see paths here.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.walks,
                        key = { it.id }
                    ) { walk ->
                        WalkPathCard(
                            walk = walk,
                            isExpanded = uiState.selectedWalkId == walk.id,
                            onClick = { viewModel.onWalkClicked(walk.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalkPathCard(
    walk: Walk,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Walk info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = walk.date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = walk.startTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.2f km", walk.distanceKm),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${walk.totalSteps} steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map (thumbnail or full based on expansion)
            if (walk.gpsPoints.isNotEmpty()) {
                val latLngPoints = walk.gpsPoints.map {
                    LatLng(it.latitude, it.longitude)
                }

                PathMapView(
                    path = latLngPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isExpanded) 400.dp else 200.dp)
                )
            }
        }
    }
}

@Composable
private fun PathMapView(
    path: List<LatLng>,
    modifier: Modifier = Modifier
) {
    if (path.isEmpty()) return

    // Calculate camera position to fit all points
    val bounds = calculateBounds(path)
    val center = LatLng(
        (bounds.first.latitude + bounds.second.latitude) / 2,
        (bounds.first.longitude + bounds.second.longitude) / 2
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 15f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    ) {
        // Draw polyline for path
        Polyline(
            points = path,
            color = androidx.compose.ui.graphics.Color(0xFF6750A4), // Material3 primary
            width = 8f
        )

        // Start marker
        Marker(
            state = MarkerState(position = path.first()),
            title = "Start",
            snippet = "Walk started here"
        )

        // End marker
        Marker(
            state = MarkerState(position = path.last()),
            title = "End",
            snippet = "Walk ended here"
        )
    }
}

private fun calculateBounds(points: List<LatLng>): Pair<LatLng, LatLng> {
    var minLat = Double.MAX_VALUE
    var maxLat = Double.MIN_VALUE
    var minLng = Double.MAX_VALUE
    var maxLng = Double.MIN_VALUE

    points.forEach { point ->
        if (point.latitude < minLat) minLat = point.latitude
        if (point.latitude > maxLat) maxLat = point.latitude
        if (point.longitude < minLng) minLng = point.longitude
        if (point.longitude > maxLng) maxLng = point.longitude
    }

    return Pair(LatLng(minLat, minLng), LatLng(maxLat, maxLng))
}
```

---

### Navigation

**Used in**: Phase 1, Task 7; Updated in Phase 2, 4, 5
**File**: `presentation/navigation/NavGraph.kt`

```kotlin
package com.example.stepeeeasy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stepeeeasy.presentation.history.HistoryScreen
import com.example.stepeeeasy.presentation.home.HomeScreen
import com.example.stepeeeasy.presentation.paths.PathsScreen
import com.example.stepeeeasy.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Paths : Screen("paths")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Paths.route) {
            PathsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
```

---

### Theme

**Used in**: All Phases
**File**: `presentation/theme/Theme.kt`

The theme file is already created by Android Studio. Make sure it includes Material 3 dynamic color support.

---

## Service Layer

### WalkTrackingService

**Used in**: Phase 3, Task 4
**File**: `service/WalkTrackingService.kt`

```kotlin
package com.example.stepeeeasy.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.stepeeeasy.MainActivity
import com.example.stepeeeasy.R
import com.example.stepeeeasy.domain.repository.IWalkRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WalkTrackingService : Service() {

    @Inject
    lateinit var walkRepository: IWalkRepository

    @Inject
    lateinit var stepSensorManager: StepSensorManager

    @Inject
    lateinit var gpsLocationManager: GpsLocationManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentWalkId: Long? = null

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "walk_tracking"

    companion object {
        const val ACTION_START_WALK = "ACTION_START_WALK"
        const val ACTION_STOP_WALK = "ACTION_STOP_WALK"
        const val EXTRA_WALK_ID = "EXTRA_WALK_ID"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_WALK -> {
                val walkId = intent.getLongExtra(EXTRA_WALK_ID, -1)
                if (walkId != -1L) {
                    startWalkTracking(walkId)
                }
            }
            ACTION_STOP_WALK -> {
                stopWalkTracking()
            }
        }

        return START_STICKY
    }

    private fun startWalkTracking(walkId: Long) {
        currentWalkId = walkId

        // Start foreground service
        startForeground(NOTIFICATION_ID, buildNotification("Walk in progress...", 0))

        // Start step counting
        stepSensorManager.startStepCounting { steps ->
            updateNotification(steps)

            // Update walk in database
            serviceScope.launch {
                walkRepository.updateWalkMetrics(walkId, steps, 0.0) // Distance updated by GPS
            }
        }

        // Start GPS tracking
        gpsLocationManager.startLocationUpdates(walkId)
    }

    private fun stopWalkTracking() {
        stepSensorManager.stopStepCounting()
        gpsLocationManager.stopLocationUpdates()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String, steps: Int): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StepEeeasy - Walking")
            .setContentText(if (steps > 0) "$steps steps" else text)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with custom icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = buildNotification("Walk in progress", steps)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Walk Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification for walk tracking"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

---

### SensorManager

**Used in**: Phase 3, Task 2
**File**: `service/StepSensorManager.kt`

```kotlin
package com.example.stepeeeasy.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var stepListener: SensorEventListener? = null
    private var initialStepCount: Int = 0
    private var currentCallback: ((Int) -> Unit)? = null

    fun startStepCounting(callback: (Int) -> Unit) {
        if (stepCounterSensor == null) {
            Log.w(TAG, "Step Counter sensor not available on this device")
            return
        }

        currentCallback = callback

        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    if (initialStepCount == 0) {
                        initialStepCount = event.values[0].toInt()
                    }

                    val currentSteps = (event.values[0].toInt() - initialStepCount).coerceAtLeast(0)
                    currentCallback?.invoke(currentSteps)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Not needed
            }
        }

        sensorManager.registerListener(
            stepListener,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        Log.d(TAG, "Step counting started")
    }

    fun stopStepCounting() {
        stepListener?.let {
            sensorManager.unregisterListener(it)
            Log.d(TAG, "Step counting stopped")
        }
        stepListener = null
        initialStepCount = 0
        currentCallback = null
    }

    companion object {
        private const val TAG = "StepSensorManager"
    }
}
```

---

### GpsLocationManager

**Used in**: Phase 3, Task 3
**File**: `service/GpsLocationManager.kt`

```kotlin
package com.example.stepeeeasy.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.stepeeeasy.domain.model.GpsPoint
import com.example.stepeeeasy.domain.repository.IWalkRepository
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsLocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walkRepository: IWalkRepository
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null
    private var currentWalkId: Long? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L // 5 seconds
    ).apply {
        setMinUpdateIntervalMillis(2000L) // Fastest: 2 seconds
        setWaitForAccurateLocation(false)
    }.build()

    fun startLocationUpdates(walkId: Long) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        currentWalkId = walkId

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Filter out low-accuracy points
                    if (location.accuracy > 50f) {
                        Log.d(TAG, "Skipping low accuracy location: ${location.accuracy}m")
                        continue
                    }

                    val gpsPoint = GpsPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = LocalDateTime.now(),
                        accuracy = location.accuracy
                    )

                    Log.d(TAG, "GPS point: ${gpsPoint.latitude}, ${gpsPoint.longitude}, accuracy: ${gpsPoint.accuracy}m")

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

        Log.d(TAG, "GPS tracking started for walk ID: $walkId")
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            Log.d(TAG, "GPS tracking stopped")
        }
        locationCallback = null
        currentWalkId = null
    }

    companion object {
        private const val TAG = "GpsLocationManager"
    }
}
```

---

## Dependency Injection

### Application Class

**Used in**: Phase 1, Task 1
**File**: `StepEeeasyApplication.kt`

```kotlin
package com.example.stepeeeasy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StepEeeasyApplication : Application()
```

---

### AppModule

**Used in**: Phase 1, Task 1
**File**: `di/AppModule.kt`

```kotlin
package com.example.stepeeeasy.di

import android.content.Context
import androidx.room.Room
import com.example.stepeeeasy.data.local.database.AppDatabase
import com.example.stepeeeasy.data.local.database.GpsPointDao
import com.example.stepeeeasy.data.local.database.WalkDao
import com.example.stepeeeasy.data.local.datastore.SettingsDataStore
import com.example.stepeeeasy.data.repository.SettingsRepositoryImpl
import com.example.stepeeeasy.data.repository.WalkRepositoryImpl
import com.example.stepeeeasy.domain.repository.ISettingsRepository
import com.example.stepeeeasy.domain.repository.IWalkRepository
import com.example.stepeeeasy.util.DistanceCalculator
import com.example.stepeeeasy.util.StrideCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "stepeeasy.db"
        )
            .fallbackToDestructiveMigration() // For development only
            .build()
    }

    @Singleton
    @Provides
    fun provideWalkDao(database: AppDatabase): WalkDao {
        return database.walkDao()
    }

    @Singleton
    @Provides
    fun provideGpsPointDao(database: AppDatabase): GpsPointDao {
        return database.gpsPointDao()
    }

    @Singleton
    @Provides
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Singleton
    @Provides
    fun provideWalkRepository(
        walkDao: WalkDao,
        gpsPointDao: GpsPointDao,
        distanceCalculator: DistanceCalculator
    ): IWalkRepository {
        return WalkRepositoryImpl(walkDao, gpsPointDao, distanceCalculator)
    }

    @Singleton
    @Provides
    fun provideSettingsRepository(
        settingsDataStore: SettingsDataStore
    ): ISettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @Singleton
    @Provides
    fun provideDistanceCalculator(): DistanceCalculator {
        return DistanceCalculator()
    }

    @Singleton
    @Provides
    fun provideStrideCalculator(): StrideCalculator {
        return StrideCalculator()
    }
}
```

---

## Utilities

### DistanceCalculator

**Used in**: Phase 3, Task 6
**File**: `util/DistanceCalculator.kt`

```kotlin
package com.example.stepeeeasy.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

class DistanceCalculator {

    /**
     * Calculate total distance from GPS points using Haversine formula
     * @param points List of GPS coordinates
     * @return Total distance in meters
     */
    fun calculateDistance(points: List<LatLng>): Double {
        if (points.size < 2) return 0.0

        var totalDistance = 0.0

        for (i in 1 until points.size) {
            totalDistance += haversineDistance(points[i - 1], points[i])
        }

        return totalDistance
    }

    /**
     * Calculate distance between two GPS points using Haversine formula
     * @param point1 First GPS coordinate
     * @param point2 Second GPS coordinate
     * @return Distance in meters
     */
    private fun haversineDistance(point1: LatLng, point2: LatLng): Double {
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_KM * c * 1000.0 // Convert to meters
    }

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
    }
}
```

---

### StrideCalculator

**Used in**: Phase 3, Task 5
**File**: `util/StrideCalculator.kt`

```kotlin
package com.example.stepeeeasy.util

class StrideCalculator {

    /**
     * Estimate stride length from user height
     * Formula: Stride (cm) = Height (cm) × 0.43
     * @param heightCm User height in centimeters
     * @return Stride length in centimeters
     */
    fun calculateStrideFromHeight(heightCm: Int): Double {
        return heightCm * STRIDE_HEIGHT_RATIO
    }

    /**
     * Calculate distance from steps and stride length
     * @param steps Number of steps taken
     * @param strideCm Stride length in centimeters
     * @return Distance in meters
     */
    fun calculateDistanceFromSteps(steps: Int, strideCm: Double): Double {
        return (steps * strideCm) / 100.0 // Convert cm to meters
    }

    /**
     * Calculate distance using user height
     * @param steps Number of steps taken
     * @param heightCm User height in centimeters
     * @return Distance in meters
     */
    fun calculateDistanceFromHeight(steps: Int, heightCm: Int): Double {
        val stride = calculateStrideFromHeight(heightCm)
        return calculateDistanceFromSteps(steps, stride)
    }

    companion object {
        private const val STRIDE_HEIGHT_RATIO = 0.43
    }
}
```

---

### DateFormatter

**Used in**: All Phases (for displaying dates/times)
**File**: `util/DateFormatter.kt`

```kotlin
package com.example.stepeeeasy.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object DateFormatter {

    /**
     * Format duration in seconds to HH:MM:SS
     */
    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    /**
     * Format LocalDate to "MMMM dd, yyyy" (e.g., "January 15, 2024")
     */
    fun formatDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return localDate.format(formatter)
    }

    /**
     * Format LocalDateTime to "h:mm a" (e.g., "3:45 PM")
     */
    fun formatTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return localDateTime.format(formatter)
    }

    /**
     * Get week range (Sunday to Saturday) for a given date
     * @return Pair of start date (Sunday) and end date (Saturday)
     */
    fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        return startOfWeek to endOfWeek
    }

    /**
     * Format week range as "MMM d - MMM d" (e.g., "Jan 14 - Jan 20")
     */
    fun formatWeekRange(startDate: LocalDate, endDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
}
```

---

## Testing

### Unit Test Example: DistanceCalculator

**Used in**: Phase 3 (testing)
**File**: `app/src/test/java/com/example/stepeeeasy/util/DistanceCalculatorTest.kt`

```kotlin
package com.example.stepeeeasy.util

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DistanceCalculatorTest {

    private lateinit var calculator: DistanceCalculator

    @Before
    fun setup() {
        calculator = DistanceCalculator()
    }

    @Test
    fun `calculate distance between two points - NYC to Times Square`() {
        val point1 = LatLng(40.7128, -74.0060) // NYC coordinates
        val point2 = LatLng(40.7580, -73.9855) // Times Square coordinates

        val distance = calculator.calculateDistance(listOf(point1, point2))

        // Distance should be approximately 5.5 km (5500 meters)
        assertTrue("Distance should be between 5000 and 6000 meters", distance in 5000.0..6000.0)
    }

    @Test
    fun `calculate distance with single point returns zero`() {
        val point = LatLng(40.7128, -74.0060)

        val distance = calculator.calculateDistance(listOf(point))

        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `calculate distance with empty list returns zero`() {
        val distance = calculator.calculateDistance(emptyList())

        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `calculate distance with multiple points accumulates correctly`() {
        val points = listOf(
            LatLng(40.7128, -74.0060),
            LatLng(40.7200, -74.0100),
            LatLng(40.7300, -74.0200)
        )

        val distance = calculator.calculateDistance(points)

        // Total distance should be sum of individual segments
        assertTrue("Distance should be greater than zero", distance > 0.0)
    }
}
```

---

### Instrumented Test Example: WalkRepository

**Used in**: Phase 1 (testing)
**File**: `app/src/androidTest/java/com/example/stepeeeasy/data/repository/WalkRepositoryTest.kt`

```kotlin
package com.example.stepeeeasy.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stepeeeasy.data.local.database.AppDatabase
import com.example.stepeeeasy.util.DistanceCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: WalkRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val distanceCalculator = DistanceCalculator()
        repository = WalkRepositoryImpl(
            database.walkDao(),
            database.gpsPointDao(),
            distanceCalculator
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun startWalk_insertsWalkToDatabase() = runBlocking {
        // When
        val walk = repository.startWalk()

        // Then
        assertNotNull(walk)
        assertTrue(walk.isActive)
        assertEquals(0, walk.totalSteps)
    }

    @Test
    fun stopWalk_updatesWalkAsInactive() = runBlocking {
        // Given
        val walk = repository.startWalk()

        // When
        val stoppedWalk = repository.stopWalk(walk.id)

        // Then
        assertNotNull(stoppedWalk.endTime)
        assertFalse(stoppedWalk.isActive)
    }

    @Test
    fun getActiveWalk_returnsActiveWalk() = runBlocking {
        // Given
        repository.startWalk()

        // When
        val activeWalk = repository.getActiveWalk().first()

        // Then
        assertNotNull(activeWalk)
        assertTrue(activeWalk!!.isActive)
    }

    @Test
    fun deleteAllWalks_clearsDatabase() = runBlocking {
        // Given
        repository.startWalk()
        repository.startWalk()

        // When
        repository.deleteAllWalks()

        // Then
        val walks = repository.getAllWalks().first()
        assertTrue(walks.isEmpty())
    }
}
```

---

## Common Patterns

### Error Handling

**Pattern**: Use try-catch in ViewModels, expose errors via UI state

```kotlin
fun onActionClicked() {
    viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Perform action
            val result = useCase()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                data = result
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error occurred"
            )
        }
    }
}
```

---

### Flow Collection in Compose

**Pattern**: Use `collectAsStateWithLifecycle()` for lifecycle-aware collection

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // UI based on uiState
}
```

---

### Permission Handling

**Pattern**: Request permissions with `rememberLauncherForActivityResult`

```kotlin
@Composable
fun MyScreen() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        // Handle result
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        ))
    }
}
```

---

### Database Transactions

**Pattern**: Room suspend functions automatically run on background thread

```kotlin
// Good - suspend function runs on IO dispatcher automatically
suspend fun insertWalk(walk: WalkEntity) {
    walkDao.insertWalk(walk) // Room handles threading
}

// Don't do this - unnecessary explicit dispatcher
suspend fun insertWalk(walk: WalkEntity) {
    withContext(Dispatchers.IO) { // Not needed
        walkDao.insertWalk(walk)
    }
}
```

---

### State Hoisting

**Pattern**: Keep state in ViewModel, pass down to Composables

```kotlin
// Good - ViewModel holds state
@Composable
fun Screen(viewModel: ViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContent(
        data = uiState.data,
        onAction = viewModel::onAction
    )
}

@Composable
private fun ScreenContent(
    data: Data,
    onAction: () -> Unit
) {
    // Stateless UI
}
```

---

## End of Technical Reference

This document contains all code examples needed to implement StepEeeasy with Jetpack Compose. Refer to `android-development-plan-v2.md` for the implementation strategy and phase ordering.

**Last Updated**: 2025-01-XX
**Version**: 2.0 (Jetpack Compose)
