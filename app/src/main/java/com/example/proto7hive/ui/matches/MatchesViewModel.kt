package com.example.proto7hive.ui.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.MatchingRepository
import com.example.proto7hive.model.MatchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MatchesUiState(
    val isLoading: Boolean = true,
    val items: List<MatchSuggestion> = emptyList(),
    val errorMessage: String? = null
)

class MatchesViewModel(private val repository: MatchingRepository, private val forUserId: String) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                _uiState.value = MatchesUiState(isLoading = false, items = repository.getSuggestions(forUserId))
            } catch (t: Throwable) {
                _uiState.value = MatchesUiState(isLoading = false, errorMessage = t.message)
            }
        }
    }
}

class MatchesViewModelFactory(private val repository: MatchingRepository, private val forUserId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchesViewModel::class.java)) {
            return MatchesViewModel(repository, forUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


