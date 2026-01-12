package com.example.proto7hive.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proto7hive.R
import com.example.proto7hive.ui.theme.BrandText
import java.util.Calendar

@Composable
fun SignUpStep4Screen(
    viewModel: SignUpViewModel,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    // Parse current date if exists, otherwise use empty strings
    var dayText by remember(state.dateOfBirth) { 
        mutableStateOf(
            state.dateOfBirth?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                cal.get(Calendar.DAY_OF_MONTH).toString()
            } ?: ""
        )
    }
    var monthText by remember(state.dateOfBirth) { 
        mutableStateOf(
            state.dateOfBirth?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                (cal.get(Calendar.MONTH) + 1).toString()
            } ?: ""
        )
    }
    var yearText by remember(state.dateOfBirth) { 
        mutableStateOf(
            state.dateOfBirth?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                cal.get(Calendar.YEAR).toString()
            } ?: ""
        )
    }
    
    // Update date when text changes
    fun updateDate(day: String, month: String, year: String) {
        if (day.isNotEmpty() && month.isNotEmpty() && year.isNotEmpty()) {
            try {
                val cal = Calendar.getInstance()
                cal.set(year.toInt(), month.toInt() - 1, day.toInt(), 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                viewModel.updateDateOfBirth(cal.timeInMillis)
            } catch (e: Exception) {
                // Invalid date, ignore
            }
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
            // Question
            Text(
                text = "What is your date of birth?",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Date Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Day Input
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull()?.let { it in 1..31 } != false)) {
                            dayText = newValue
                            updateDate(dayText, monthText, yearText)
                        }
                    },
                    placeholder = { Text("DD", color = Color(0xFF999999)) },
                    label = { Text("Day", color = Color(0xFF999999)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999),
                        focusedLabelColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF999999),
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Month Input
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull()?.let { it in 1..12 } != false)) {
                            monthText = newValue
                            updateDate(dayText, monthText, yearText)
                        }
                    },
                    placeholder = { Text("MM", color = Color(0xFF999999)) },
                    label = { Text("Month", color = Color(0xFF999999)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999),
                        focusedLabelColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF999999),
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Year Input
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 4)) {
                            yearText = newValue
                            updateDate(dayText, monthText, yearText)
                        }
                    },
                    placeholder = { Text("YYYY", color = Color(0xFF999999)) },
                    label = { Text("Year", color = Color(0xFF999999)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999),
                        focusedLabelColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF999999),
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info Text
            Text(
                text = "Enter your date of birth (e.g., 22 09 2002).",
                color = Color(0xFF999999),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Next Button
            val isFormValid = state.dateOfBirth != null
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFormValid) Color(0xFFCCCCCC) else Color(0xFF888888),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        if (isFormValid) {
                            onNext()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Next",
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

