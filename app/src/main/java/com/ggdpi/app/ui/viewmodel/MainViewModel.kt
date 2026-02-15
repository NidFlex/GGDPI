package com.ggdpi.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggdpi.app.core.StrategyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val strategyManager: StrategyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            strategyManager.currentStrategy.collect { strategy ->
                _uiState.value = _uiState.value.copy(currentStrategy = strategy.displayName)
            }
        }
    }

    fun selectStrategy(strategyId: StrategyManager.StrategyId) {
        viewModelScope.launch {
            strategyManager.setStrategy(strategyId)
        }
    }

    data class MainUiState(
        val isActive: Boolean = false,
        val currentStrategy: String = "General",
        val packetsProcessed: Long = 0,
        val bypassSuccess: Boolean = false
    )
}