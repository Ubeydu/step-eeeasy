// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.github.ben-manes.versions") version "0.53.0"
}

buildscript {
    configurations.classpath {
        resolutionStrategy.force("com.squareup:javapoet:1.13.0")
        // Pin plugin classpath: AGP 8.13.0 pulls javapoet:1.10.0, which lacks ClassName.canonicalName().
        // Hilt’s AggregateDepsTask needs ≥ 1.13.0. Remove when AGP stops bringing 1.10.0.
    }
}
