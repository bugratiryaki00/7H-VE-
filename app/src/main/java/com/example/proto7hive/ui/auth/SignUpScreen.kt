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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proto7hive.R
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.ui.theme.BrandText
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository()))
) {
    val state by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Handle success
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onSignUpSuccess()
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
            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "Enter E-Mail",
                        color = Color(0xFF999999)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = Color(0xFF999999),
                    unfocusedPlaceholderColor = Color(0xFF999999),
                    cursorColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "Enter Password",
                        color = Color(0xFF999999)
                    )
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = Color(0xFF999999),
                    unfocusedPlaceholderColor = Color(0xFF999999),
                    cursorColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = {
                    Text(
                        text = "Confirm Password",
                        color = Color(0xFF999999)
                    )
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = Color(0xFF999999),
                    unfocusedPlaceholderColor = Color(0xFF999999),
                    cursorColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            val passwordsMatch = password == confirmPassword && password.isNotEmpty()
            val isFormValid = email.isNotEmpty() && password.isNotEmpty() && passwordsMatch
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFormValid) Color(0xFFCCCCCC) else Color(0xFF888888),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        enabled = !state.isLoading && isFormValid
                    ) {
                        if (passwordsMatch) {
                            authViewModel.createUserWithEmail(email, password)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        text = "Submit",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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

