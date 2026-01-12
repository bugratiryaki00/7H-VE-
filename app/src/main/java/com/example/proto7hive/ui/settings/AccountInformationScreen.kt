package com.example.proto7hive.ui.settings

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.R
import com.example.proto7hive.ui.theme.BrandYellow
import java.io.File
import java.util.UUID

@Composable
fun AccountInformationRoute(
    onBack: () -> Unit,
    viewModel: AccountInformationViewModel = viewModel(factory = AccountInformationViewModelFactory())
) {
    AccountInformationScreen(
        onBack = onBack,
        viewModel = viewModel
    )
}

@Composable
fun AccountInformationScreen(
    onBack: () -> Unit,
    viewModel: AccountInformationViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Temporary file for camera
    val photoFile = remember {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        File(cacheDir, "temp_photo_${UUID.randomUUID()}.jpg")
    }
    val photoUri = remember(photoFile) {
        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
        } catch (e: Exception) {
            Log.e("AccountInformationScreen", "FileProvider hatası: ${e.message}", e)
            android.net.Uri.EMPTY
        }
    }

    // Gallery launcher
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.setProfileImage(selectedUri)
        }
    }

    // Camera launcher
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.setProfileImage(photoUri)
        }
    }

    // Camera permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && photoUri != android.net.Uri.EMPTY) {
            takePicture.launch(photoUri)
        }
    }

    // Başarılı kayıt sonrası geri dön
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.clearSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            // Header with Back Arrow and Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_logo_7hive),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .height(32.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Title "Account Information"
            Text(
                text = "Account Information",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profil Resmi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(
                                    width = 2.dp,
                                    color = BrandYellow,
                                    shape = CircleShape
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageToShow = state.profileImageUri ?: state.profileImageUrl?.let { Uri.parse(it) }
                            if (imageToShow != null) {
                                AsyncImage(
                                    model = imageToShow,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = BrandYellow,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }

                    // Add and Take Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Add Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Galeri",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Take Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (photoUri != android.net.Uri.EMPTY) {
                                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Kamera",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("İsim", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Surname
            OutlinedTextField(
                value = state.surname,
                onValueChange = { viewModel.updateSurname(it) },
                label = { Text("Soyisim", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Email (Read-only)
            OutlinedTextField(
                value = state.email,
                onValueChange = { },
                label = { Text("E-posta", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                enabled = false
            )

            // Department
            OutlinedTextField(
                value = state.department,
                onValueChange = { viewModel.updateDepartment(it) },
                label = { Text("Bölüm", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Bio
            OutlinedTextField(
                value = state.bio,
                onValueChange = { viewModel.updateBio(it) },
                label = { Text("Bio", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveChanges() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !state.isSaving && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Kaydet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error Message
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.typography.bodyMedium.color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Success Message
            if (state.saveSuccess) {
                Text(
                    text = "Değişiklikler kaydedildi",
                    color = BrandYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
