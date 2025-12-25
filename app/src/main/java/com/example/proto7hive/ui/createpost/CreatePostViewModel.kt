package com.example.proto7hive.ui.createpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.PostRepository
import com.example.proto7hive.data.StorageRepository
import com.example.proto7hive.data.JobRepository
import com.example.proto7hive.data.FirestoreJobRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Job
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val text: String = "",
    val imageUri: Uri? = null, // Seçilen görsel URI'si (henüz yüklenmemiş)
    val imageUrl: String? = null, // Firebase Storage'dan gelen URL (yüklenmiş)
    val postType: String = "post", // "post" veya "work"
    // Work alanları
    val workTitle: String = "",
    val workCompany: String = "",
    val workLocation: String = "",
    val workType: String = "", // Full-time, Part-time, Remote, Hybrid, On-site
    val workDescription: String = "",
    val workRequiredSkills: List<String> = emptyList(),
    val isUploadingImage: Boolean = false,
    val isPosting: Boolean = false,
    val errorMessage: String? = null,
    val postSuccess: Boolean = false
)

class CreatePostViewModel(
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
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

    fun setPostType(type: String) {
        // Post type değiştiğinde görsel state'ini temizle
        _uiState.value = _uiState.value.copy(
            postType = type,
            imageUri = null,
            imageUrl = null
        )
    }

    // Work alanları için update fonksiyonları
    fun updateWorkTitle(title: String) {
        _uiState.value = _uiState.value.copy(workTitle = title)
    }

    fun updateWorkCompany(company: String) {
        _uiState.value = _uiState.value.copy(workCompany = company)
    }

    fun updateWorkLocation(location: String) {
        _uiState.value = _uiState.value.copy(workLocation = location)
    }

    fun updateWorkType(workType: String) {
        _uiState.value = _uiState.value.copy(workType = workType)
    }

    fun updateWorkDescription(description: String) {
        _uiState.value = _uiState.value.copy(workDescription = description)
    }

    fun addWorkSkill(skill: String) {
        val currentSkills = _uiState.value.workRequiredSkills.toMutableList()
        if (!currentSkills.contains(skill.trim())) {
            currentSkills.add(skill.trim())
            _uiState.value = _uiState.value.copy(workRequiredSkills = currentSkills)
        }
    }

    fun removeWorkSkill(skill: String) {
        val currentSkills = _uiState.value.workRequiredSkills.toMutableList()
        currentSkills.remove(skill)
        _uiState.value = _uiState.value.copy(workRequiredSkills = currentSkills)
    }

    suspend fun uploadImage(): String? {
        val imageUri = _uiState.value.imageUri ?: return null

        _uiState.value = _uiState.value.copy(
            isUploadingImage = true,
            errorMessage = null
        )

        return try {
            android.util.Log.d("CreatePostViewModel", "Görsel yükleniyor: $imageUri")
            val downloadUrl = storageRepository.uploadPostImage(imageUri)
            android.util.Log.d("CreatePostViewModel", "Görsel başarıyla yüklendi: $downloadUrl")
            _uiState.value = _uiState.value.copy(
                imageUrl = downloadUrl,
                isUploadingImage = false
            )
            downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("CreatePostViewModel", "Görsel yükleme hatası", e)
            android.util.Log.e("CreatePostViewModel", "Hata mesajı: ${e.message}")
            android.util.Log.e("CreatePostViewModel", "Hata tipi: ${e.javaClass.simpleName}")
            _uiState.value = _uiState.value.copy(
                isUploadingImage = false,
                errorMessage = "Görsel yüklenirken hata: ${e.message}"
            )
            null
        }
    }

    fun sharePost(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Giriş yapmamış kullanıcı"
            )
            return
        }

        // Validation
        if (currentState.postType == "work") {
            // Work için validasyon
            if (currentState.workTitle.isBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "İş başlığı boş olamaz"
                )
                return
            }
            if (currentState.workCompany.isBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "Şirket adı boş olamaz"
                )
                return
            }
            if (currentState.workType.isBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "Çalışma tipi seçilmelidir"
                )
                return
            }
        } else {
            // Post için validasyon - text veya image olmalı
            val text = currentState.text.trim()
            if (text.isEmpty() && currentState.imageUri == null) {
                _uiState.value = currentState.copy(
                    errorMessage = "Post metni veya görsel eklemelisiniz"
                )
                return
            }
        }

        _uiState.value = currentState.copy(
            isPosting = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Önce görsel varsa yükle
                var finalImageUrl = currentState.imageUrl
                android.util.Log.d("CreatePostViewModel", "sharePost başladı - imageUri: ${currentState.imageUri}, imageUrl: $finalImageUrl")
                if (currentState.imageUri != null && finalImageUrl == null) {
                    android.util.Log.d("CreatePostViewModel", "Görsel yükleme başlatılıyor...")
                    finalImageUrl = uploadImage()
                    android.util.Log.d("CreatePostViewModel", "Görsel yükleme sonucu: $finalImageUrl")
                    if (finalImageUrl == null && currentState.imageUri != null) {
                        val errorMsg = _uiState.value.errorMessage ?: "Bilinmeyen hata"
                        android.util.Log.e("CreatePostViewModel", "Görsel yüklenemedi, paylaşım iptal ediliyor. Hata: $errorMsg")
                        _uiState.value = _uiState.value.copy(
                            isPosting = false,
                            errorMessage = "Görsel yüklenemedi: $errorMsg"
                        )
                        return@launch
                    }
                }

                if (currentState.postType == "work") {
                    // Work paylaşımı: Hem Post hem Job oluştur
                    val post = Post(
                        id = "",
                        userId = currentUser.uid,
                        text = currentState.workDescription.ifBlank { "${currentState.workTitle} – ${currentState.workCompany}" },
                        imageUrl = finalImageUrl,
                        timestamp = System.currentTimeMillis(),
                        postType = "work"
                    )

                    val job = Job(
                        id = "",
                        title = currentState.workTitle,
                        company = currentState.workCompany,
                        location = currentState.workLocation,
                        workType = currentState.workType,
                        description = currentState.workDescription,
                        requiredSkills = currentState.workRequiredSkills,
                        imageUrl = finalImageUrl,
                        userId = currentUser.uid
                    )

                    android.util.Log.d("CreatePostViewModel", "Work oluşturuluyor: title=${job.title}, company=${job.company}")
                    
                    // Hem post hem job oluştur
                    val postId = postRepository.createPost(post)
                    val jobId = jobRepository.createJob(job)
                    
                    android.util.Log.d("CreatePostViewModel", "Work başarıyla oluşturuldu: postId=$postId, jobId=$jobId")
                } else {
                    // Normal post paylaşımı
                    val text = currentState.text.trim()
                    val post = Post(
                        id = "",
                        userId = currentUser.uid,
                        text = text,
                        imageUrl = finalImageUrl,
                        timestamp = System.currentTimeMillis(),
                        postType = "post"
                    )

                    android.util.Log.d("CreatePostViewModel", "Post oluşturuluyor: userId=${post.userId}, text length=${post.text.length}")
                    
                    val postId = postRepository.createPost(post)
                    android.util.Log.d("CreatePostViewModel", "Post başarıyla oluşturuldu: postId=$postId")
                }
                
                _uiState.value = CreatePostUiState(postSuccess = true)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CreatePostViewModel", "Paylaşım hatası", e)
                val errorMsg = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> 
                        "Yetki hatası: Firestore Security Rules kontrol edin"
                    e.message?.contains("UNAUTHENTICATED") == true -> 
                        "Giriş yapmamış kullanıcı. Lütfen tekrar giriş yapın"
                    else -> e.message ?: "Paylaşılamadı: ${e.javaClass.simpleName}"
                }
                _uiState.value = currentState.copy(
                    isPosting = false,
                    errorMessage = errorMsg
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
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(postRepository, jobRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

