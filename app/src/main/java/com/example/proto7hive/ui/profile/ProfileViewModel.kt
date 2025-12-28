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
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val jobs: List<Job> = emptyList(),
    val connectionsCount: Int = 0,
    val isConnected: Boolean = false, // Current user bu kullanıcıyı takip ediyor mu?
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userId: String? = null, // null ise current user, değilse belirtilen user
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    
    fun addConnection(targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    connectionRepository.addConnection(currentUser.uid, targetUserId)
                    // Refresh profile to update connections count
                    loadProfile()
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Bağlantı eklenirken hata oluştu"
                )
            }
        }
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

                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = user,
                    posts = posts,
                    jobs = jobs,
                    connectionsCount = connectionsCount,
                    isConnected = isConnected
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
}

class ProfileViewModelFactory(
    private val userId: String? = null,
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userId, userRepository, postRepository, jobRepository, connectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

