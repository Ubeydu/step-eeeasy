package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.repository.WalkRepository
import javax.inject.Inject

/**
 * Use case for clearing all recorded walks.
 *
 * Deletes all walks and their GPS points from the database.
 * Used when user taps "Clear Recorded Walks" button in Settings.
 *
 * GPS points are automatically deleted due to CASCADE foreign key constraint.
 */
class ClearAllWalksUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    /**
     * Delete all walks from database.
     *
     * This is a destructive operation and should be called only after user confirmation.
     */
    suspend operator fun invoke() {
        walkRepository.deleteAllWalks()
    }
}
