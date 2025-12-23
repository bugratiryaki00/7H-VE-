package com.example.proto7hive.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.data.AuthResult
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    // Step 1: Basic Info
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val userType: String? = null, // Student, Staff, Academician, Graduate
    
    // Step 2: OTP
    val otpCode: String = "",
    val otpSent: Boolean = false,
    
    // Step 3: Gender
    val gender: String? = null, // Male, Female, The Other, I don't want to specify
    
    // Step 4: Date of Birth
    val dateOfBirth: Long? = null, // Unix timestamp
    
    // Step 5: Password
    val password: String = "",
    
    // General
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpComplete: Boolean = false
)

class SignUpViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun updateSurname(surname: String) {
        _uiState.value = _uiState.value.copy(surname = surname)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updateUserType(userType: String) {
        _uiState.value = _uiState.value.copy(userType = userType)
    }
    
    fun updateOtpCode(code: String) {
        _uiState.value = _uiState.value.copy(otpCode = code)
    }
    
    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }
    
    fun updateDateOfBirth(timestamp: Long) {
        _uiState.value = _uiState.value.copy(dateOfBirth = timestamp)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < 5) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1, errorMessage = null)
        }
    }
    
    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1, errorMessage = null)
        }
    }
    
    fun sendOtp() {
        val state = _uiState.value
        if (state.email.isEmpty()) {
            return
        }
        
        // TEST MODE: Gerçek OTP gönderilmiyor
        // Production'da Firebase Cloud Functions ile gerçek OTP gönderilecek
        // Şu an için sadece state'i güncelliyoruz
        _uiState.value = state.copy(
            isLoading = false,
            otpSent = true
        )
    }
    
    fun verifyOtp(): Boolean {
        // TEST MODE: Herhangi bir 6 haneli kod geçerli
        // Production'da Firebase Cloud Functions ile gerçek OTP doğrulaması yapılacak
        val state = _uiState.value
        return state.otpCode.length == 6
    }
    
    fun completeSignUp() {
        val state = _uiState.value
        if (state.password.isEmpty() || state.email.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Lütfen tüm alanları doldurun")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // 1. Firebase Authentication'da kullanıcı oluştur
                val authResult = authRepository.createUserWithEmail(state.email, state.password)
                
                if (!authResult.success || authResult.user == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpComplete = false,
                        errorMessage = authResult.message ?: "Kullanıcı oluşturulamadı"
                    )
                    return@launch
                }
                
                val firebaseUser = authResult.user
                val userId = firebaseUser.uid
                
                // 2. Firestore'a user dokümanı oluştur
                try {
                    val user = User(
                        id = userId,
                        name = state.name,
                        surname = state.surname,
                        email = state.email,
                        userType = state.userType,
                        gender = state.gender,
                        dateOfBirth = state.dateOfBirth,
                        department = null, // İlk kayıtta boş, sonra doldurulabilir
                        skills = emptyList(),
                        interests = emptyList(),
                        badges = emptyList(),
                        availability = null,
                        connections = emptyList(),
                        profileImageUrl = null,
                        bio = null
                    )
                    
                    userRepository.updateUser(user)
                } catch (e: Exception) {
                    // Firestore'a yazma hatası
                    val errorMsg = e.message ?: "Bilinmeyen hata"
                    android.util.Log.e("SignUpViewModel", "Firestore'a kullanıcı yazılamadı: $errorMsg", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpComplete = false,
                        errorMessage = "Kullanıcı kaydedilemedi: $errorMsg\n\nFirebase Console'da Firestore Security Rules'u kontrol edin."
                    )
                    return@launch
                }
                
                // 3. Email verification gönder (herhangi bir email için çalışır)
                try {
                    val verificationResult = authRepository.sendEmailVerification()
                    if (!verificationResult.success) {
                        android.util.Log.w("SignUpViewModel", "Email verification gönderilemedi: ${verificationResult.message}")
                        // Email verification başarısız olsa bile signup devam eder
                    }
                } catch (e: Exception) {
                    // Email verification hatası signup'ı durdurmaz
                    android.util.Log.w("SignUpViewModel", "Email verification hatası: ${e.message}")
                }
                
                // 4. Başarılı - signup tamamlandı
                android.util.Log.d("SignUpViewModel", "Signup başarılı, isSignUpComplete = true yapılıyor")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSignUpComplete = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSignUpComplete = false,
                    errorMessage = e.message ?: "Bir hata oluştu"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

