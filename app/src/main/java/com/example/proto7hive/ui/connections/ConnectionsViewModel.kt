package com.example.proto7hive.ui.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.ConnectionRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ConnectionsUiState(
    val isLoading: Boolean = true,
    val suggestedConnections: List<User> = emptyList(),
    val myConnections: List<User> = emptyList(),
    val errorMessage: String? = null
)

class ConnectionsViewModel(
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionsUiState())
    val uiState: StateFlow<ConnectionsUiState> = _uiState

    init {
        loadConnections()
    }

    fun loadConnections() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = ConnectionsUiState(
                        isLoading = false,
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }

                val suggested = connectionRepository.getSuggestedConnections(currentUser.uid)
                val myConnections = connectionRepository.getConnectionUsers(currentUser.uid)

                _uiState.value = ConnectionsUiState(
                    isLoading = false,
                    suggestedConnections = suggested,
                    myConnections = myConnections
                )
            } catch (t: Throwable) {
                _uiState.value = ConnectionsUiState(
                    isLoading = false,
                    errorMessage = t.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun addConnection(userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                connectionRepository.addConnection(currentUser.uid, userId)
                loadConnections() // Refresh list
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Bağlantı eklenirken hata oluştu"
                )
            }
        }
    }

    fun removeConnection(userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                connectionRepository.removeConnection(currentUser.uid, userId)
                loadConnections() // Refresh list
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Bağlantı kaldırılırken hata oluştu"
                )
            }
        }
    }

    fun refresh() {
        loadConnections()
    }
}

class ConnectionsViewModelFactory(
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionsViewModel::class.java)) {
            return ConnectionsViewModel(connectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

