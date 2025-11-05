package com.example.stepeeeasy.domain.usecase

import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.repository.WalkRepository
import javax.inject.Inject

/**
 * Use case for starting a new walk session.
 */
class StartWalkUseCase @Inject constructor(
    private val walkRepository: WalkRepository
) {
    suspend operator fun invoke(): Walk {
        return walkRepository.startWalk()
    }
}
