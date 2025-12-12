package com.example.proto7hive.ui.createpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.PostRepository
import com.example.proto7hive.data.StorageRepository
import com.example.proto7hive.model.Post
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val text: String = "",
    val imageUri: Uri? = null, // Seçilen görsel URI'si (henüz yüklenmemiş)
    val imageUrl: String? = null, // Firebase Storage'dan gelen URL (yüklenmiş)
    val isUploadingImage: Boolean = false,
    val isPosting: Boolean = false,
    val errorMessage: String? = null,
    val postSuccess: Boolean = false
)

class CreatePostViewModel(
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState

    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(text = text)
    }

    fun setSelectedImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            imageUri = uri,
            imageUrl = null // Henüz yüklenmedi
        )
    }

    fun removeImage() {
        _uiState.value = _uiState.value.copy(
            imageUri = null,
            imageUrl = null
        )
    }

    suspend fun uploadImage(): String? {
        val imageUri = _uiState.value.imageUri ?: return null

        _uiState.value = _uiState.value.copy(
            isUploadingImage = true,
            errorMessage = null
        )

        return try {
            val downloadUrl = storageRepository.uploadPostImage(imageUri)
            _uiState.value = _uiState.value.copy(
                imageUrl = downloadUrl,
                isUploadingImage = false
            )
            downloadUrl
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isUploadingImage = false,
                errorMessage = "Görsel yüklenirken hata: ${e.message}"
            )
            null
        }
    }

    fun sharePost(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val text = currentState.text.trim()

        if (text.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Post metni boş olamaz"
            )
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Giriş yapmamış kullanıcı"
            )
            return
        }

        _uiState.value = currentState.copy(
            isPosting = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Önce görsel varsa yükle
                var finalImageUrl = currentState.imageUrl
                if (currentState.imageUri != null && finalImageUrl == null) {
                    finalImageUrl = uploadImage()
                    if (finalImageUrl == null && currentState.imageUri != null) {
                        // Görsel yüklenemedi ama devam edebiliriz
                        _uiState.value = currentState.copy(
                            isPosting = false,
                            errorMessage = "Görsel yüklenemedi, tekrar deneyin"
                        )
                        return@launch
                    }
                }

                val post = Post(
                    id = "", // Firestore otomatik ID oluşturacak
                    userId = currentUser.uid,
                    text = text,
                    imageUrl = finalImageUrl,
                    timestamp = System.currentTimeMillis()
                )

                postRepository.createPost(post)
                
                _uiState.value = CreatePostUiState(postSuccess = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isPosting = false,
                    errorMessage = e.message ?: "Post paylaşılamadı"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = CreatePostUiState()
    }
}

class CreatePostViewModelFactory(
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(postRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

