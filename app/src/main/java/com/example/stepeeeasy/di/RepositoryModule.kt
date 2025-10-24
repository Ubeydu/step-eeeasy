package com.example.stepeeeasy.di

import com.example.stepeeeasy.data.repository.SettingsRepositoryImpl
import com.example.stepeeeasy.data.repository.WalkRepositoryImpl
import com.example.stepeeeasy.domain.repository.SettingsRepository
import com.example.stepeeeasy.domain.repository.WalkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository dependencies.
 *
 * Key Concepts:
 * - @Module tells Hilt this class contains dependency bindings
 * - @InstallIn(SingletonComponent::class) means these live as long as the app
 * - @Binds is used to map an INTERFACE to its IMPLEMENTATION
 *
 * Why @Binds instead of @Provides?
 * - @Binds is more efficient (less generated code)
 * - @Binds is specifically for interface → implementation mapping
 * - @Provides is for creating objects manually
 *
 * Example usage:
 * When a ViewModel needs WalkRepository:
 * ```
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val walkRepository: WalkRepository  // Hilt will inject WalkRepositoryImpl
 * ) : ViewModel()
 * ```
 *
 * Hilt sees:
 * 1. HomeViewModel needs WalkRepository (interface)
 * 2. Looks in modules and finds @Binds binding WalkRepository → WalkRepositoryImpl
 * 3. Creates WalkRepositoryImpl (automatically injecting WalkDao and GpsPointDao)
 * 4. Injects it into HomeViewModel
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds WalkRepository interface to WalkRepositoryImpl implementation.
     *
     * This tells Hilt: "Whenever someone needs a WalkRepository, give them WalkRepositoryImpl"
     *
     * @param implementation The concrete implementation (WalkRepositoryImpl)
     * @return The interface type (WalkRepository)
     *
     * How it works:
     * - WalkRepositoryImpl has @Inject constructor, so Hilt knows how to create it
     * - WalkRepositoryImpl needs WalkDao and GpsPointDao, which are provided by DatabaseModule
     * - Hilt automatically chains the dependencies together
     * - Result: Full dependency injection with zero manual wiring!
     *
     * Note: This must be an ABSTRACT function in an ABSTRACT class when using @Binds
     */
    @Binds
    @Singleton
    abstract fun bindWalkRepository(
        implementation: WalkRepositoryImpl
    ): WalkRepository

    /**
     * Binds SettingsRepository interface to SettingsRepositoryImpl implementation.
     *
     * This tells Hilt: "Whenever someone needs a SettingsRepository, give them SettingsRepositoryImpl"
     *
     * @param implementation The concrete implementation (SettingsRepositoryImpl)
     * @return The interface type (SettingsRepository)
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl
    ): SettingsRepository
}
