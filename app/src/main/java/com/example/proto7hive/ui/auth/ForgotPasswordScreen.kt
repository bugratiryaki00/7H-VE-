package com.example.proto7hive.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proto7hive.R
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.ui.theme.BrandText
import com.example.proto7hive.ui.theme.BrandYellow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null
)

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun sendPasswordResetEmail() {
        val state = _uiState.value
        if (state.email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.value = state.copy(errorMessage = "Lütfen geçerli bir e-posta adresi girin")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, message = null)
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(state.email)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isEmailSent = result.success,
                message = result.message,
                errorMessage = if (!result.success) result.message else null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle messages
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle errors
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Forgot Password?",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Description
            Text(
                text = "Enter your email address and we'll send you a link to reset your password.",
                color = Color(0xFF999999),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Input
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.updateEmail(it) },
                placeholder = {
                    Text(
                        text = "Enter E-Mail",
                        color = Color(0xFF999999)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !state.isEmailSent && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = Color(0xFF999999),
                    unfocusedPlaceholderColor = Color(0xFF999999),
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = Color(0xFF888888),
                    disabledBorderColor = Color(0xFF555555)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send Button
            val isFormValid = state.email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFormValid && !state.isEmailSent && !state.isLoading) Color(0xFFCCCCCC) else Color(0xFF888888),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = isFormValid && !state.isEmailSent && !state.isLoading) {
                        viewModel.sendPasswordResetEmail()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else if (state.isEmailSent) {
                    Text(
                        text = "Email Sent",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Send Reset Link",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.isEmailSent) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Check your email for the password reset link.",
                    color = BrandYellow,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back to Login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to Login",
                    color = BrandYellow,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Logo at bottom
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.size(120.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

