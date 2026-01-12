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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proto7hive.R
import com.example.proto7hive.ui.theme.BrandText
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun SignUpStep2Screen(
    viewModel: SignUpViewModel,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        if (!state.otpSent) {
            viewModel.sendOtp()
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
            // Instruction Text
            Text(
                text = "Enter the 6-digit OTP code in the Yeditepe email extension.",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Test Mode Notice
            Text(
                text = "(Test Mode: Enter any 6-digit code)",
                color = Color(0xFF999999),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // OTP Input
            OutlinedTextField(
                value = state.otpCode,
                onValueChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { char -> char.isDigit() }) {
                        viewModel.updateOtpCode(newValue)
                    }
                },
                placeholder = {
                    Text(
                        text = "Enter OTP Code",
                        color = Color(0xFF999999)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            
            // Submit Button
            val isFormValid = state.otpCode.length == 6
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFormValid) Color(0xFFCCCCCC) else Color(0xFF888888),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        if (isFormValid && viewModel.verifyOtp()) {
                            onNext()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Submit",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
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
    }
}

