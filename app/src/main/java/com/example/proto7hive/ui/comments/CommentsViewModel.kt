package com.example.proto7hive.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.CommentRepository
import com.example.proto7hive.data.FirestoreCommentRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.model.Comment
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val users: Map<String, User> = emptyMap(), // comment.userId -> User mapping
    val newCommentText: String = "",
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val errorMessage: String? = null
)

class CommentsViewModel(
    private val commentRepository: CommentRepository = FirestoreCommentRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository(),
    val postId: String? = null,
    val jobId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState

    init {
        loadComments()
    }

    fun loadComments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val comments = if (postId != null) {
                    commentRepository.getCommentsByPostId(postId)
                } else if (jobId != null) {
                    commentRepository.getCommentsByJobId(jobId)
                } else {
                    emptyList()
                }

                // Kullanıcı bilgilerini çek
                val userIds = comments.map { it.userId }.distinct()
                val users = userRepository.getUsers(userIds).associateBy { it.id }

                _uiState.value = _uiState.value.copy(
                    comments = comments,
                    users = users,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Yorumlar yüklenemedi: ${e.message}"
                )
            }
        }
    }

    fun updateCommentText(text: String) {
        _uiState.value = _uiState.value.copy(newCommentText = text)
    }

    fun postComment() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Giriş yapmamış kullanıcı"
            )
            return
        }

        val text = _uiState.value.newCommentText.trim()
        if (text.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true, errorMessage = null)

            try {
                val comment = Comment(
                    id = "",
                    postId = postId,
                    jobId = jobId,
                    userId = currentUser.uid,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                commentRepository.createComment(comment)
                
                // Yorumu ekledikten sonra listeyi yeniden yükle
                loadComments()
                
                _uiState.value = _uiState.value.copy(
                    newCommentText = "",
                    isPosting = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPosting = false,
                    errorMessage = "Yorum gönderilemedi: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class CommentsViewModelFactory(
    private val postId: String?,
    private val jobId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            return CommentsViewModel(postId = postId, jobId = jobId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

