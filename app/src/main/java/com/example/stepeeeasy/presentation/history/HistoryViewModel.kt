package com.example.stepeeeasy.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.domain.usecase.GetAllWalksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for History screen.
 *
 * Responsibilities:
 * - Load all completed walks using GetAllWalksUseCase
 * - Expose walks as StateFlow for UI to collect
 * - Handle empty state
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    getAllWalksUseCase: GetAllWalksUseCase
) : ViewModel() {

    /**
     * StateFlow of all completed walks.
     * Automatically updates when walks are added/removed.
     * Empty list when no walks exist.
     */
    val walks: StateFlow<List<Walk>> = getAllWalksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
