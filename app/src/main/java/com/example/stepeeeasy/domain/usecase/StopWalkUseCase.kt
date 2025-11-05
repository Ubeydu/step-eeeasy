package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import javax.inject.Inject

/**
 * Use case for stopping the active walk session.
 */
class StopWalkUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    suspend operator fun invoke(totalSteps: Int, distanceMeters: Double): Walk? {
        require(totalSteps >= 0) { "Step count cannot be negative" }
        require(distanceMeters >= 0) { "Distance cannot be negative" }
        return walkRepository.stopWalk(totalSteps, distanceMeters)
    }
}
