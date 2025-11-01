package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for fetching all completed walks.
 *
 * Filters out active walks (is_active = true) to only show completed walks.
 * Walks are already sorted by start_time DESC in the DAO.
 */
class GetAllWalksUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    /**
     * Get all completed walks as a Flow.
     *
     * @return Flow emitting list of completed walks, sorted newest first
     */
    operator fun invoke(): Flow<List<Walk>> {
        return walkRepository.getAllWalks()
            .map { walks ->
                walks.filter { !it.isActive }  // Exclude active walk
            }
    }
}
