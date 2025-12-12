package com.example.proto7hive.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.PostRepository
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.ConnectionRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeFeedUiState(
    val isLoading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val users: Map<String, User> = emptyMap(), // userId -> User mapping
    val errorMessage: String? = null
)

class HomeFeedViewModel(
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val connectionRepository: ConnectionRepository = FirestoreConnectionRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeFeedUiState())
    val uiState: StateFlow<HomeFeedUiState> = _uiState

    init {
        loadPosts()
    }

    fun loadPosts() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = HomeFeedUiState(
                        isLoading = false,
                        posts = emptyList(),
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }

                // Kullanıcının bağlantılarını al ve sadece bağlantıların postlarını göster
                val connections = connectionRepository.getConnections(currentUser.uid)
                val connectionIds = connections + currentUser.uid // Kendi postlarını da ekle
                val posts = if (connectionIds.isEmpty()) {
                    // Bağlantı yoksa sadece kendi postlarını göster
                    postRepository.getPostsByUserId(currentUser.uid)
                } else {
                    postRepository.getPostsByUserIds(connectionIds)
                }
                
                // Post'lardaki kullanıcı bilgilerini çek
                val userIds = posts.map { it.userId }.distinct()
                val userList = if (userIds.isNotEmpty()) {
                    userRepository.getUsers(userIds)
                } else {
                    emptyList()
                }
                val userMap = userList.associateBy { it.id }
                
                _uiState.value = HomeFeedUiState(
                    isLoading = false,
                    posts = posts,
                    users = userMap
                )
            } catch (t: Throwable) {
                _uiState.value = HomeFeedUiState(
                    isLoading = false,
                    posts = emptyList(),
                    errorMessage = t.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun refresh() {
        loadPosts()
    }
}

class HomeFeedViewModelFactory(
    private val postRepository: PostRepository,
    private val connectionRepository: ConnectionRepository,
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFeedViewModel::class.java)) {
            return HomeFeedViewModel(postRepository, connectionRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

