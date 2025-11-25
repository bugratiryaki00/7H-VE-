package com.example.proto7hive.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.ui.auth.AuthViewModel
import com.example.proto7hive.ui.auth.AuthViewModelFactory

@Composable
fun OnboardingRoute(
    onContinue: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository()))
) {
    val state by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = "7hive'e Hoş Geldiniz", style = MaterialTheme.typography.titleLarge)
        Text(
            text = if (isSignUp) "Hesap oluşturun" else "Giriş yapın",
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta (@yeditepe.edu.tr)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            isError = !email.endsWith("@yeditepe.edu.tr", ignoreCase = true) && email.isNotEmpty()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isSignUp) {
                        authViewModel.createUserWithEmail(email, password)
                    } else {
                        authViewModel.signInWithEmail(email, password)
                    }
                },
                enabled = !state.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (isSignUp) "Hesap Oluştur" else "Giriş Yap")
            }
        }

        TextButton(
            onClick = { isSignUp = !isSignUp },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(if (isSignUp) "Zaten hesabınız var mı? Giriş yapın" else "Hesabınız yok mu? Oluşturun")
        }

        if (state.isSignedIn) {
            Button(
                onClick = onContinue,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Devam Et")
            }
        }

        state.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        state.message?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}