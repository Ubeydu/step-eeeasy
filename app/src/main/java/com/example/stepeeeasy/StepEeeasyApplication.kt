package com.example.stepeeeasy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class for StepEeeasy.
 *
 * This class is instantiated before any other class when the app starts.
 * It serves as the entry point for Hilt dependency injection.
 *
 * Key Concepts:
 * - @HiltAndroidApp triggers Hilt's code generation
 * - Hilt will generate a base class that sets up the dependency injection container
 * - This container holds all your dependencies (database, repositories, etc.)
 * - You don't need to write any code in this class - the annotation does everything
 *
 * Think of it like:
 * This is where Hilt sets up its "vending machine" full of dependencies.
 * When other parts of your app need something (like a database),
 * they can ask Hilt and it will provide it from this container.
 */
@HiltAndroidApp
class StepEeeasyApplication : Application() {
    // No code needed here!
    // Hilt handles everything through the @HiltAndroidApp annotation.

    // Optional: You could add app-wide initialization here if needed
    // For example: logging setup, crash reporting, etc.
    // But for our app, this is all we need.
}
