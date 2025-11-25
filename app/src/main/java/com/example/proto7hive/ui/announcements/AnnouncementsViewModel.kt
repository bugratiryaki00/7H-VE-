package com.example.proto7hive.ui.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.AnnouncementRepository
import com.example.proto7hive.model.Announcement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AnnouncementsUiState(
    val isLoading: Boolean = true,
    val items: List<Announcement> = emptyList(),
    val errorMessage: String? = null
)

class AnnouncementsViewModel(private val repository: AnnouncementRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                _uiState.value = AnnouncementsUiState(isLoading = false, items = repository.getAnnouncements())
            } catch (t: Throwable) {
                _uiState.value = AnnouncementsUiState(isLoading = false, errorMessage = t.message)
            }
        }
    }
}

class AnnouncementsViewModelFactory(private val repository: AnnouncementRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementsViewModel::class.java)) {
            return AnnouncementsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


