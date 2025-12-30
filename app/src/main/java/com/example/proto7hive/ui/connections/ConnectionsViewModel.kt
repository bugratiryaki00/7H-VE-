package com.example.proto7hive.ui.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.ConnectionRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.data.NotificationRepository
import com.example.proto7hive.data.FirestoreNotificationRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.model.User
import com.example.proto7hive.model.Notification
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
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository(),
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
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

    fun sendConnectionRequest(targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                // İstek gönder
                val requestId = connectionRepository.sendConnectionRequest(currentUser.uid, targetUserId)
                
                // İstek alan kullanıcıya bildirim gönder
                val currentUserData = userRepository.getUser(currentUser.uid)
                if (currentUserData != null) {
                    val notification = Notification(
                        id = "",
                        userId = targetUserId, // İsteği alan kullanıcı
                        fromUserId = currentUser.uid, // İsteği gönderen
                        type = "FOLLOW_REQUEST",
                        relatedId = requestId,
                        relatedType = "connection_request",
                        message = "${currentUserData.name} ${currentUserData.surname} sent you a connection request",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    notificationRepository.createNotification(notification)
                }
                
                loadConnections() // Refresh list
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Connection request gönderilirken hata oluştu"
                )
            }
        }
    }
    
    fun addConnection(userId: String) {
        // Eski fonksiyon, geriye dönük uyumluluk için bırakıyoruz ama sendConnectionRequest'i kullanıyoruz
        sendConnectionRequest(userId)
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
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository(),
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionsViewModel::class.java)) {
            return ConnectionsViewModel(connectionRepository, notificationRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

