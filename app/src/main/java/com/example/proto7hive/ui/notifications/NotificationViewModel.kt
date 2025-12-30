package com.example.proto7hive.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.FirestoreNotificationRepository
import com.example.proto7hive.data.NotificationRepository
import com.example.proto7hive.data.ConnectionRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.model.Notification
import com.example.proto7hive.model.User
import com.example.proto7hive.data.FirestoreUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationUiState(
    val isLoading: Boolean = true,
    val allNotifications: List<NotificationWithUser> = emptyList(),
    val commentNotifications: List<NotificationWithUser> = emptyList(),
    val followerNotifications: List<NotificationWithUser> = emptyList(),
    val inviteNotifications: List<NotificationWithUser> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null
)

data class NotificationWithUser(
    val notification: Notification,
    val fromUser: User?
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository(),
    private val userRepository: FirestoreUserRepository = FirestoreUserRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = NotificationUiState(
                        isLoading = false,
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }

                // Tüm bildirimleri getir
                val allNotifications = notificationRepository.getNotifications(currentUser.uid)
                
                // Kullanıcı bilgilerini getir
                val userIds = allNotifications.map { it.fromUserId }.distinct()
                val users = if (userIds.isNotEmpty()) {
                    userRepository.getUsers(userIds)
                } else {
                    emptyList()
                }
                val userMap = users.associateBy { it.id }

                // NotificationWithUser listelerini oluştur
                // Accepted connection request'leri için notification'ları filtrele
                // Her notification'ın relatedId'sini kontrol ederek accepted request'leri bul
                val notificationsWithUsers = allNotifications
                    .filter { notification ->
                        // FOLLOW_REQUEST tipindeki notification'ları kontrol et
                        if (notification.type == "FOLLOW_REQUEST" && notification.relatedId != null) {
                            // Connection request'in status'unu kontrol et
                            // Eğer accepted ise, notification'ı gösterme
                            try {
                                // Firestore'dan request'i direkt kontrol et
                                val request = FirebaseFirestore.getInstance()
                                    .collection("connectionRequests")
                                    .document(notification.relatedId!!)
                                    .get()
                                    .await()
                                val status = request.data?.get("status") as? String
                                status != "accepted"
                            } catch (e: Exception) {
                                // Hata olursa notification'ı göster
                                true
                            }
                        } else {
                            true
                        }
                    }
                    .map { notification ->
                        NotificationWithUser(
                            notification = notification,
                            fromUser = userMap[notification.fromUserId]
                        )
                    }

                // Tip'e göre filtrele
                val commentNotifications = notificationsWithUsers.filter { 
                    it.notification.type == "COMMENT" 
                }
                val followerNotifications = notificationsWithUsers.filter { 
                    it.notification.type == "FOLLOW_REQUEST" 
                }
                val inviteNotifications = notificationsWithUsers.filter { 
                    it.notification.type == "INVITE" 
                }

                // Unread count'u hesapla
                val unreadCount = notificationsWithUsers.count { !it.notification.isRead }
                
                _uiState.value = NotificationUiState(
                    isLoading = false,
                    allNotifications = notificationsWithUsers,
                    commentNotifications = commentNotifications,
                    followerNotifications = followerNotifications,
                    inviteNotifications = inviteNotifications,
                    unreadCount = unreadCount
                )
            } catch (t: Throwable) {
                _uiState.value = NotificationUiState(
                    isLoading = false,
                    errorMessage = t.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
                loadNotifications() // Refresh
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Bildirim okundu olarak işaretlenirken hata oluştu"
                )
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                notificationRepository.markAllAsRead(currentUser.uid)
                loadNotifications() // Refresh
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Tüm bildirimler okundu olarak işaretlenirken hata oluştu"
                )
            }
        }
    }

    fun refresh() {
        loadNotifications()
    }

    fun acceptConnectionRequest(notificationId: String, requestId: String?) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                // İsteği kabul et
                if (requestId != null) {
                    connectionRepository.acceptConnectionRequest(requestId)
                }
                
                // Bildirimi okundu olarak işaretle
                notificationRepository.markAsRead(notificationId)
                
                // Bildirimleri yenile
                loadNotifications()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Connection request kabul edilirken hata oluştu"
                )
            }
        }
    }

    fun rejectConnectionRequest(notificationId: String, requestId: String?) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                // İsteği reddet
                if (requestId != null) {
                    connectionRepository.rejectConnectionRequest(requestId)
                }
                
                // Bildirimi okundu olarak işaretle
                notificationRepository.markAsRead(notificationId)
                
                // Bildirimleri yenile
                loadNotifications()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Connection request reddedilirken hata oluştu"
                )
            }
        }
    }
}

class NotificationViewModelFactory(
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository(),
    private val userRepository: FirestoreUserRepository = FirestoreUserRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(notificationRepository, userRepository, connectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

