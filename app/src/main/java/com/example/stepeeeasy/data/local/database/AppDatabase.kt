package com.example.stepeeeasy.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stepeeeasy.data.local.dao.WalkDao
import com.example.stepeeeasy.data.local.entity.WalkEntity

/**
 * The Room database for the StepEeeasy app.
 *
 * This is the main entry point for all database access.
 * Room will automatically generate the implementation of this class.
 *
 * Key Concepts:
 * - @Database annotation tells Room this is a database class
 * - entities = [...] lists all the tables in the database
 * - version = 2 is the database schema version (incremented after removing GPS tracking)
 * - exportSchema = false means we don't export the schema to a file (optional for dev)
 */
@Database(
    entities = [
        WalkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to Walk table operations.
     *
     * Room will automatically generate the implementation.
     * You don't need to write any code - just declare the abstract method.
     *
     * Usage:
     * ```
     * val walkDao = database.walkDao()
     * val walks = walkDao.getAllWalks()
     * ```
     */
    abstract fun walkDao(): WalkDao

    companion object {
        /**
         * The name of the database file.
         *
         * This will create a file called "stepeeeasy.db" in the app's private storage.
         * You can view this database using Android Studio's Database Inspector or adb.
         */
        const val DATABASE_NAME = "stepeeeasy.db"
    }
}