package com.example.proto7hive.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.StorageRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileSettingsUiState(
    val name: String = "",
    val surname: String = "",
    val department: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val profileImageUri: Uri? = null, // Seçilen görsel URI'si (henüz yüklenmemiş)
    val profileImageUrl: String? = null, // Firebase Storage'dan gelen URL (yüklenmiş)
    val isSaving: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false
)

class ProfileSettingsViewModel(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState: StateFlow<ProfileSettingsUiState> = _uiState

    fun loadUserData(user: User) {
        _uiState.value = ProfileSettingsUiState(
            name = user.name,
            surname = user.surname,
            department = user.department ?: "",
            bio = user.bio ?: "",
            skills = user.skills,
            profileImageUrl = user.profileImageUrl
        )
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateSurname(surname: String) {
        _uiState.value = _uiState.value.copy(surname = surname)
    }

    fun updateDepartment(department: String) {
        _uiState.value = _uiState.value.copy(department = department)
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(bio = bio)
    }

    fun addSkill(skill: String) {
        val currentSkills = _uiState.value.skills.toMutableList()
        if (!currentSkills.contains(skill)) {
            currentSkills.add(skill)
            _uiState.value = _uiState.value.copy(skills = currentSkills)
        }
    }

    fun removeSkill(skill: String) {
        val currentSkills = _uiState.value.skills.toMutableList()
        currentSkills.remove(skill)
        _uiState.value = _uiState.value.copy(skills = currentSkills)
    }

    fun setProfileImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            profileImageUri = uri,
            profileImageUrl = null // Henüz yüklenmedi
        )
    }

    suspend fun uploadProfileImage(): String? {
        val imageUri = _uiState.value.profileImageUri
        if (imageUri == null) {
            Log.w("ProfileSettingsViewModel", "uploadProfileImage çağrıldı ama profileImageUri null")
            return null
        }

        Log.d("ProfileSettingsViewModel", "uploadProfileImage başladı, URI: $imageUri")
        _uiState.value = _uiState.value.copy(
            isUploadingImage = true,
            errorMessage = null
        )

        return try {
            Log.d("ProfileSettingsViewModel", "storageRepository.uploadProfileImage çağrılıyor...")
            val downloadUrl = storageRepository.uploadProfileImage(imageUri)
            Log.d("ProfileSettingsViewModel", "Görsel başarıyla yüklendi, URL: $downloadUrl")
            _uiState.value = _uiState.value.copy(
                profileImageUrl = downloadUrl,
                isUploadingImage = false
            )
            downloadUrl
        } catch (e: Exception) {
            Log.e("ProfileSettingsViewModel", "Görsel yükleme hatası", e)
            _uiState.value = _uiState.value.copy(
                isUploadingImage = false,
                errorMessage = "Görsel yüklenirken hata: ${e.message}"
            )
            null
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Giriş yapmamış kullanıcı"
            )
            return
        }

        if (currentState.name.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Ad boş olamaz"
            )
            return
        }

        _uiState.value = currentState.copy(
            isSaving = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Önce görsel varsa yükle
                var finalImageUrl = currentState.profileImageUrl
                if (currentState.profileImageUri != null && finalImageUrl == null) {
                    finalImageUrl = uploadProfileImage()
                    if (finalImageUrl == null && currentState.profileImageUri != null) {
                        _uiState.value = currentState.copy(
                            isSaving = false,
                            errorMessage = "Görsel yüklenemedi, tekrar deneyin"
                        )
                        return@launch
                    }
                }

                // Mevcut kullanıcı bilgilerini al (diğer alanları korumak için)
                val existingUser = userRepository.getUser(currentUser.uid)
                
                // Kullanıcı bilgilerini güncelle (mevcut bilgileri koru)
                val updatedUser = existingUser?.copy(
                    name = currentState.name,
                    surname = currentState.surname,
                    department = currentState.department.ifBlank { null },
                    bio = currentState.bio.ifBlank { null },
                    skills = currentState.skills,
                    profileImageUrl = finalImageUrl ?: currentState.profileImageUrl ?: existingUser.profileImageUrl
                ) ?: User(
                    id = currentUser.uid,
                    name = currentState.name,
                    surname = currentState.surname,
                    email = currentUser.email ?: "",
                    department = currentState.department.ifBlank { null },
                    bio = currentState.bio.ifBlank { null },
                    skills = currentState.skills,
                    profileImageUrl = finalImageUrl ?: currentState.profileImageUrl
                )

                userRepository.updateUser(updatedUser)

                // Güncellenmiş kullanıcı bilgilerini tekrar yükle
                val updatedUserFromDb = userRepository.getUser(currentUser.uid)
                _uiState.value = if (updatedUserFromDb != null) {
                    ProfileSettingsUiState(
                        updateSuccess = true,
                        name = updatedUserFromDb.name,
                        surname = updatedUserFromDb.surname,
                        department = updatedUserFromDb.department ?: "",
                        bio = updatedUserFromDb.bio ?: "",
                        skills = updatedUserFromDb.skills,
                        profileImageUrl = updatedUserFromDb.profileImageUrl,
                        isSaving = false
                    )
                } else {
                    ProfileSettingsUiState(
                        updateSuccess = true,
                        name = currentState.name,
                        surname = currentState.surname,
                        department = currentState.department,
                        bio = currentState.bio,
                        skills = currentState.skills,
                        profileImageUrl = finalImageUrl ?: currentState.profileImageUrl,
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Profil güncellenirken hata oluştu"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}

class ProfileSettingsViewModelFactory(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileSettingsViewModel::class.java)) {
            return ProfileSettingsViewModel(userRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

