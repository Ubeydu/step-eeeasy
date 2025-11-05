package com.example.stepeeeasy.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stepeeeasy.data.local.dao.WalkDao
import com.example.stepeeeasy.data.local.entity.WalkEntity

/**
 * Room database for the app.
 */
@Database(
    entities = [
        WalkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walkDao(): WalkDao

    companion object {
        const val DATABASE_NAME = "stepeeeasy.db"
    }
}