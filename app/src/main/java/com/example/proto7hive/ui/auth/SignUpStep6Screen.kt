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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.proto7hive.ui.theme.BrandText
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@Composable
fun SignUpStep6Screen(
    viewModel: SignUpViewModel,
    onSignUpComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle sign up completion
    LaunchedEffect(state.isSignUpComplete) {
        if (state.isSignUpComplete) {
            onSignUpComplete()
        }
    }
    
    // Handle error messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
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
                text = "Enter Password",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Password Input
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.updatePassword(it) },
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Next Button
            val isFormValid = state.password.isNotEmpty()
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFormValid) Color(0xFFCCCCCC) else Color(0xFF888888),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        if (isFormValid && !state.isLoading) {
                            viewModel.completeSignUp()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        text = "Next",
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
        
        // Snackbar for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

