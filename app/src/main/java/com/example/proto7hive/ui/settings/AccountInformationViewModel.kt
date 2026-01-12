package com.example.proto7hive.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.data.StorageRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AccountInformationUiState(
    val user: User? = null,
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val bio: String = "",
    val department: String = "",
    val selectedBadge: String? = null, // JOB, INT, TEAM, HIRING, MENTOR
    val profileImageUri: Uri? = null, // Yeni seçilen görsel (henüz yüklenmemiş)
    val profileImageUrl: String? = null, // Mevcut profil resmi URL'i
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class AccountInformationViewModel(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountInformationUiState())
    val uiState: StateFlow<AccountInformationUiState> = _uiState

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        name = user.name,
                        surname = user.surname,
                        email = user.email,
                        bio = user.bio ?: "",
                        department = user.department ?: "",
                        selectedBadge = user.badges.firstOrNull(),
                        profileImageUrl = user.profileImageUrl,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                        errorMessage = "Error loading data: ${e.message}"
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateSurname(surname: String) {
        _uiState.value = _uiState.value.copy(surname = surname)
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(bio = bio)
    }

    fun updateDepartment(department: String) {
        _uiState.value = _uiState.value.copy(department = department)
    }

    fun updateBadge(badge: String?) {
        _uiState.value = _uiState.value.copy(selectedBadge = badge)
    }

    fun setProfileImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    fun saveChanges() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "User not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                val state = _uiState.value
                var profileImageUrl = state.profileImageUrl

                // Yeni profil resmi seçilmişse yükle
                if (state.profileImageUri != null) {
                    profileImageUrl = storageRepository.uploadProfileImage(state.profileImageUri)
                }

                // Kullanıcı bilgilerini güncelle
                val badges = if (state.selectedBadge != null) listOf(state.selectedBadge) else emptyList()
                val updatedUser = state.user!!.copy(
                    name = state.name.trim(),
                    surname = state.surname.trim(),
                    bio = if (state.bio.isNotBlank()) state.bio.trim() else null,
                    department = if (state.department.isNotBlank()) state.department.trim() else null,
                    badges = badges,
                    profileImageUrl = profileImageUrl
                )

                userRepository.updateUser(updatedUser)

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    profileImageUrl = profileImageUrl,
                    profileImageUri = null, // Yüklendikten sonra temizle
                    user = updatedUser
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                        errorMessage = "Error saving: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}

class AccountInformationViewModelFactory(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountInformationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountInformationViewModel(userRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
