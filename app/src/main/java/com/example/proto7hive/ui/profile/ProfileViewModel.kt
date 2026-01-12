package com.example.proto7hive.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.PostRepository
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.JobRepository
import com.example.proto7hive.data.FirestoreJobRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.data.ConnectionRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.data.NotificationRepository
import com.example.proto7hive.data.FirestoreNotificationRepository
import com.example.proto7hive.data.CollectionRepository
import com.example.proto7hive.data.FirestoreCollectionRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.User
import com.example.proto7hive.model.Notification
import com.example.proto7hive.model.Collection
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val jobs: List<Job> = emptyList(),
    val collections: List<Collection> = emptyList(), // Kullanıcının koleksiyonları
    val selectedCollectionJobs: List<Job> = emptyList(), // Seçilen koleksiyondaki job'lar
    val selectedCollectionPosts: List<Post> = emptyList(), // Seçilen koleksiyondaki post'lar (work'ler)
    val selectedCollectionId: String? = null, // Seçilen koleksiyon ID'si (null ise koleksiyon listesi gösterilir)
    val connectionsCount: Int = 0,
    val isConnected: Boolean = false, // Current user bu kullanıcıyı takip ediyor mu?
    val connectionRequestStatus: String? = null, // null, "pending" (gönderildi, bekliyor), "sent" (gönderildi)
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userId: String? = null, // null ise current user, değilse belirtilen user
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository(),
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository(),
    private val collectionRepository: CollectionRepository = FirestoreCollectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    
    fun sendConnectionRequest(targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }
                
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
                
                // Profile'ı yenile (connectionRequestStatus güncellenecek)
                loadProfile()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Connection request gönderilirken hata oluştu"
                )
            }
        }
    }
    
    fun addConnection(targetUserId: String) {
        // Eski fonksiyon, artık kullanılmıyor ama geriye dönük uyumluluk için bırakıyoruz
        sendConnectionRequest(targetUserId)
    }

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val targetUserId = userId ?: run {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        _uiState.value = ProfileUiState(
                            isLoading = false,
                            errorMessage = "Giriş yapmamış kullanıcı"
                        )
                        return@launch
                    }
                    currentUser.uid
                }

                // Kullanıcı bilgilerini çek
                val user = userRepository.getUser(targetUserId)
                
                if (user == null) {
                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        errorMessage = "Kullanıcı bulunamadı"
                    )
                    return@launch
                }
                
                // Post'ları çek
                val posts = postRepository.getPostsByUserId(targetUserId)
                
                // Jobs'ları çek
                val jobs = jobRepository.getJobsByUserId(targetUserId)
                
                // Koleksiyonları çek
                val collections = collectionRepository.getCollectionsByUserId(targetUserId)
                
                // Bağlantı sayısı
                val connections = connectionRepository.getConnections(targetUserId)
                val connectionsCount = connections.size
                
                // Current user bu kullanıcıyı takip ediyor mu?
                val currentUser = FirebaseAuth.getInstance().currentUser
                val isConnected = if (currentUser != null && userId != null && currentUser.uid != targetUserId) {
                    val currentUserConnections = connectionRepository.getConnections(currentUser.uid)
                    targetUserId in currentUserConnections
                } else {
                    false
                }
                
                // Connection request durumu
                val connectionRequestStatus: String? = if (currentUser != null && userId != null && currentUser.uid != targetUserId && !isConnected) {
                    val sentRequests = connectionRepository.getSentRequests(currentUser.uid)
                    // Eğer current user bu kullanıcıya istek göndermişse "pending"
                    if (sentRequests.any { it.toUserId == targetUserId }) {
                        "pending"
                    } else {
                        null
                    }
                } else {
                    null
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    posts = posts,
                    jobs = jobs,
                    collections = collections,
                    connectionsCount = connectionsCount,
                    isConnected = isConnected,
                    connectionRequestStatus = connectionRequestStatus,
                    selectedCollectionId = null, // Reset when loading profile
                    selectedCollectionJobs = emptyList(), // Reset when loading profile
                    selectedCollectionPosts = emptyList() // Reset when loading profile
                )
            } catch (t: Throwable) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    errorMessage = t.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun refresh() {
        loadProfile()
    }
    
    fun selectCollection(collectionId: String?) {
        if (collectionId == null) {
            _uiState.value = _uiState.value.copy(
                selectedCollectionId = null,
                selectedCollectionJobs = emptyList(),
                selectedCollectionPosts = emptyList()
            )
        } else {
            viewModelScope.launch {
                try {
                    val jobs = collectionRepository.getJobsByCollectionId(collectionId)
                    // Job'lara karşılık gelen Post'ları bul (postType="work" ve aynı userId/imageUrl'e sahip olanlar)
                    val currentState = _uiState.value
                    val jobImageUrls = jobs.mapNotNull { it.imageUrl }.toSet()
                    val matchingPosts = currentState.posts.filter { post ->
                        post.postType == "work" &&
                        post.userId == currentState.user?.id &&
                        (post.imageUrl in jobImageUrls || (post.imageUrl == null && jobs.any { it.imageUrl == null }))
                    }.sortedByDescending { it.timestamp }
                    
                    _uiState.value = _uiState.value.copy(
                        selectedCollectionId = collectionId,
                        selectedCollectionJobs = jobs,
                        selectedCollectionPosts = matchingPosts
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error loading collection: ${e.message}"
                    )
                }
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userId: String? = null,
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository(),
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository(),
    private val collectionRepository: CollectionRepository = FirestoreCollectionRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userId, userRepository, postRepository, jobRepository, connectionRepository, notificationRepository, collectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

