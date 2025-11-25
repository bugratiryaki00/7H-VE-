package com.example.proto7hive.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.MockPortfolioRepository
import com.example.proto7hive.data.PortfolioRepository
import com.example.proto7hive.model.PortfolioCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PortfolioUiState(
    val isLoading: Boolean = true,
    val items: List<PortfolioCard> = emptyList(),
    val errorMessage: String? = null,
)

class PortfolioViewModel(
    private val repository: PortfolioRepository = MockPortfolioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val data = repository.getPortfolios()
                _uiState.value = PortfolioUiState(isLoading = false, items = data)
            } catch (t: Throwable) {
                _uiState.value = PortfolioUiState(isLoading = false, items = emptyList(), errorMessage = t.message)
            }
        }
    }
}

class PortfolioViewModelFactory(
    private val repository: PortfolioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


