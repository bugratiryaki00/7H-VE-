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
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }

                // Kullanıcı bilgilerini çek
                val user = userRepository.getUser(currentUser.uid)
                
                // Post'ları çek
                val posts = postRepository.getPostsByUserId(currentUser.uid)
                
                // Jobs'ları çek
                val jobs = jobRepository.getJobsByUserId(currentUser.uid)
                
                // Bağlantı sayısı
                val connections = connectionRepository.getConnections(currentUser.uid)
                val connectionsCount = connections.size

                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = user,
                    posts = posts,
                    jobs = jobs,
                    connectionsCount = connectionsCount
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
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository, postRepository, jobRepository, connectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

