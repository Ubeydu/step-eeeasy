package com.example.stepeeeasy.di

import android.content.Context
import androidx.room.Room
import com.example.stepeeeasy.data.local.dao.GpsPointDao
import com.example.stepeeeasy.data.local.dao.WalkDao
import com.example.stepeeeasy.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 *
 * Key Concepts:
 * - @Module tells Hilt this class contains dependency providers
 * - @InstallIn(SingletonComponent::class) means these dependencies live as long as the app
 * - @Provides tells Hilt "this method creates a dependency"
 * - @Singleton means only ONE instance will be created and shared everywhere
 *
 * Think of this like:
 * This is the "recipe book" that tells Hilt how to create things.
 * Each @Provides method is a recipe for one dependency.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the AppDatabase instance.
     *
     * This is the main database object. It will be created once and reused everywhere.
     *
     * @param context The application context (Hilt provides this automatically)
     * @return The AppDatabase instance
     *
     * Explanation:
     * - @ApplicationContext tells Hilt to inject the app-level Context
     * - Room.databaseBuilder() creates the database
     * - .fallbackToDestructiveMigration() means if schema changes, drop and recreate
     *   (Fine for development, but be careful in production!)
     * - .build() actually creates the database
     *
     * When someone needs the database, Hilt will:
     * 1. Check if it already created one
     * 2. If yes, return the existing one
     * 3. If no, call this method to create it, then return it
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // For development - recreates DB on schema changes
            .build()
    }

    /**
     * Provides the WalkDao.
     *
     * WalkDao is obtained from the database, so this method takes the database as a parameter.
     * Hilt will automatically provide the database (using provideAppDatabase above).
     *
     * @param database The AppDatabase (provided by Hilt)
     * @return The WalkDao instance
     *
     * Explanation:
     * - This method depends on AppDatabase
     * - Hilt sees the parameter and automatically calls provideAppDatabase() to get it
     * - Then we just return database.walkDao()
     * - No @Singleton here because the DAO is lightweight and tied to the database
     */
    @Provides
    fun provideWalkDao(database: AppDatabase): WalkDao {
        return database.walkDao()
    }

    /**
     * Provides the GpsPointDao.
     *
     * Same concept as provideWalkDao - gets the DAO from the database.
     *
     * @param database The AppDatabase (provided by Hilt)
     * @return The GpsPointDao instance
     */
    @Provides
    fun provideGpsPointDao(database: AppDatabase): GpsPointDao {
        return database.gpsPointDao()
    }
}