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
import com.example.proto7hive.data.CollectionRepository
import com.example.proto7hive.data.FirestoreCollectionRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.Collection
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
    val isJobPosting: Boolean? = null, // null = henüz seçilmedi, true = çalışan arıyor, false = kişisel iş
    val workTitle: String = "",
    val workCompany: String = "",
    val workLocation: String = "",
    val workType: String = "", // Full-time, Part-time, Remote, Hybrid, On-site
    val workDescription: String = "",
    val workRequiredSkills: List<String> = emptyList(),
    val selectedCollectionId: String? = null, // Seçilen koleksiyon ID'si
    val collections: List<Collection> = emptyList(), // Kullanıcının koleksiyonları
    val isUploadingImage: Boolean = false,
    val isPosting: Boolean = false,
    val errorMessage: String? = null,
    val postSuccess: Boolean = false
)

class CreatePostViewModel(
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val storageRepository: StorageRepository = StorageRepository(),
    private val collectionRepository: CollectionRepository = FirestoreCollectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState

    init {
        loadCollections()
    }

    private fun loadCollections() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val collections = collectionRepository.getCollectionsByUserId(currentUser.uid)
                    _uiState.value = _uiState.value.copy(collections = collections)
                } catch (e: Exception) {
                    android.util.Log.e("CreatePostViewModel", "Koleksiyonlar yüklenirken hata", e)
                }
            }
        }
    }

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
            imageUrl = null,
            isJobPosting = if (type == "work") null else null // Work seçilince null'a dön
        )
    }

    fun setIsJobPosting(isJobPosting: Boolean) {
        _uiState.value = _uiState.value.copy(isJobPosting = isJobPosting)
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

    fun updateSelectedCollection(collectionId: String?) {
        _uiState.value = _uiState.value.copy(selectedCollectionId = collectionId)
    }

    fun refreshCollections() {
        loadCollections()
    }

    fun createCollection(name: String, onSuccess: (String) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Collection name cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            try {
                val collection = com.example.proto7hive.model.Collection(
                    id = "",
                    name = name.trim(),
                    userId = currentUser.uid,
                    thumbnailUrl = null,
                    createdAt = System.currentTimeMillis()
                )
                val collectionId = collectionRepository.createCollection(collection)
                loadCollections() // Koleksiyonları yeniden yükle
                onSuccess(collectionId)
            } catch (e: Exception) {
                android.util.Log.e("CreatePostViewModel", "Koleksiyon oluşturma hatası", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to create collection: ${e.message}"
                )
            }
        }
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
                errorMessage = "Error uploading image: ${e.message}"
            )
            null
        }
    }

    fun sharePost(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = currentState.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        // Validation
        if (currentState.postType == "work") {
            if (currentState.isJobPosting == null) {
                _uiState.value = currentState.copy(
                        errorMessage = "Please select if you are looking for employees"
                )
                return
            }
            
            if (currentState.selectedCollectionId == null) {
                _uiState.value = currentState.copy(
                    errorMessage = "Collection must be selected"
                )
                return
            }
            
            if (currentState.isJobPosting == true) {
                // Çalışan arıyor - Basic Information zorunlu
                if (currentState.workTitle.isBlank()) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Job title cannot be empty"
                    )
                    return
                }
                if (currentState.workCompany.isBlank()) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Company name cannot be empty"
                    )
                    return
                }
                if (currentState.workType.isBlank()) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Work type must be selected"
                    )
                    return
                }
            } else {
                // Kişisel iş - Sadece görsel veya metin (ikisi de zorunlu değil, ama en az biri olmalı)
                if (currentState.imageUri == null && currentState.text.isBlank()) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Please add at least one image or text"
                    )
                    return
                }
            }
        } else {
            // Post için validasyon - text veya image olmalı
            val text = currentState.text.trim()
            if (text.isEmpty() && currentState.imageUri == null) {
                _uiState.value = currentState.copy(
                        errorMessage = "Please add post text or image"
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
                        val errorMsg = _uiState.value.errorMessage ?: "Unknown error"
                        android.util.Log.e("CreatePostViewModel", "Görsel yüklenemedi, paylaşım iptal ediliyor. Hata: $errorMsg")
                        _uiState.value = _uiState.value.copy(
                            isPosting = false,
                            errorMessage = "Failed to upload image: $errorMsg"
                        )
                        return@launch
                    }
                }

                if (currentState.postType == "work") {
                    // Work paylaşımı: Hem Post hem Job oluştur
                    val postText = if (currentState.isJobPosting == true) {
                        currentState.workDescription.ifBlank { "${currentState.workTitle} – ${currentState.workCompany}" }
                    } else {
                        currentState.text.trim()
                    }
                    
                    val post = Post(
                        id = "",
                        userId = currentUser.uid,
                        text = postText,
                        imageUrl = finalImageUrl,
                        timestamp = System.currentTimeMillis(),
                        postType = "work"
                    )

                    val job = if (currentState.isJobPosting == true) {
                        // Çalışan arıyor - tam Job bilgileri
                        Job(
                            id = "",
                            title = currentState.workTitle,
                            company = currentState.workCompany,
                            location = currentState.workLocation,
                            workType = currentState.workType,
                            description = currentState.workDescription,
                            requiredSkills = currentState.workRequiredSkills,
                            imageUrl = finalImageUrl,
                            userId = currentUser.uid,
                            collectionId = currentState.selectedCollectionId,
                            isJobPosting = true
                        )
                    } else {
                        // Kişisel iş - minimal Job bilgileri
                        Job(
                            id = "",
                            title = "",
                            company = "",
                            location = "",
                            workType = "",
                            description = currentState.text.trim(),
                            requiredSkills = emptyList(),
                            imageUrl = finalImageUrl,
                            userId = currentUser.uid,
                            collectionId = currentState.selectedCollectionId,
                            isJobPosting = false
                        )
                    }

                    android.util.Log.d("CreatePostViewModel", "Work oluşturuluyor: title=${job.title}, company=${job.company}")
                    
                    // Hem post hem job oluştur
                    val postId = postRepository.createPost(post)
                    val jobId = jobRepository.createJob(job)
                    
                    // Eğer koleksiyonun thumbnail'i yoksa ve work'in resmi varsa, koleksiyonun thumbnail'ini güncelle
                    if (currentState.selectedCollectionId != null && finalImageUrl != null && finalImageUrl.isNotBlank()) {
                        val collection = collectionRepository.getCollection(currentState.selectedCollectionId)
                        if (collection != null && (collection.thumbnailUrl == null || collection.thumbnailUrl.isBlank())) {
                            val updatedCollection = collection.copy(thumbnailUrl = finalImageUrl)
                            collectionRepository.updateCollection(updatedCollection)
                            android.util.Log.d("CreatePostViewModel", "Koleksiyon thumbnail'i güncellendi: ${collection.name}")
                        }
                    }
                    
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
                        "Permission error: Please check Firestore Security Rules"
                    e.message?.contains("UNAUTHENTICATED") == true -> 
                        "User not logged in. Please sign in again"
                    else -> e.message ?: "Failed to share: ${e.javaClass.simpleName}"
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
    private val storageRepository: StorageRepository = StorageRepository(),
    private val collectionRepository: CollectionRepository = FirestoreCollectionRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(postRepository, jobRepository, storageRepository, collectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

