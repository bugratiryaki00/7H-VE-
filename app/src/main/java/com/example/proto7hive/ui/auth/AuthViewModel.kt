package com.example.proto7hive.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.data.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val user: FirebaseUser? = null,
    val message: String? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(
            isSignedIn = user != null,
            isEmailVerified = authRepository.isEmailVerified(),
            user = user
        )
    }

    fun signInWithEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSignedIn = result.success,
                isEmailVerified = authRepository.isEmailVerified(),
                user = result.user,
                message = result.message,
                errorMessage = if (!result.success) result.message else null
            )
        }
    }

    fun createUserWithEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.createUserWithEmail(email, password)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSignedIn = result.success,
                isEmailVerified = authRepository.isEmailVerified(),
                user = result.user,
                message = result.message,
                errorMessage = if (!result.success) result.message else null
            )
        }
    }

    fun sendEmailVerification() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = result.message,
                errorMessage = if (!result.success) result.message else null
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, errorMessage = null)
    }
    
    fun resetSignInState() {
        // UI state'ini resetle, ama Firebase'den çıkış yapma
        _uiState.value = _uiState.value.copy(isSignedIn = false, user = null)
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
