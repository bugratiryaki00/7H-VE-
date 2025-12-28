package com.example.proto7hive.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.R
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun ProfileSettingsRoute(
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory()
    )
    val settingsViewModel: ProfileSettingsViewModel = viewModel(
        factory = ProfileSettingsViewModelFactory()
    )
    
    ProfileSettingsScreen(
        profileViewModel = profileViewModel,
        settingsViewModel = settingsViewModel,
        onBack = onBack,
        onProfileUpdated = onProfileUpdated,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    profileViewModel: ProfileViewModel,
    settingsViewModel: ProfileSettingsViewModel,
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Profil güncellendiğinde callback
    LaunchedEffect(settingsState.updateSuccess) {
        if (settingsState.updateSuccess) {
            snackbarHostState.showSnackbar(
                message = "Profil başarıyla güncellendi",
                duration = SnackbarDuration.Short
            )
            profileViewModel.refresh()
            onProfileUpdated()
            settingsViewModel.resetUpdateSuccess()
        }
    }

    // Hata mesajını göster
    LaunchedEffect(settingsState.errorMessage) {
        settingsState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            settingsViewModel.clearError()
        }
    }

    // İlk yüklemede ve profil güncellendiğinde mevcut kullanıcı bilgilerini yükle
    LaunchedEffect(profileState.user) {
        profileState.user?.let { user ->
            settingsViewModel.loadUserData(user)
        }
    }

    Scaffold(
        topBar = {
            Surface(color = BrandBackgroundDark) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profili Düzenle",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = BrandBackgroundDark,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Profile Picture Section
            item(key = "profile_picture") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Picture
                        Box(
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BrandYellow.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    settingsState.profileImageUri != null -> {
                                        AsyncImage(
                                            model = settingsState.profileImageUri,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    !settingsState.profileImageUrl.isNullOrBlank() -> {
                                        AsyncImage(
                                            model = settingsState.profileImageUrl!!,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    profileState.user?.profileImageUrl != null && profileState.user?.profileImageUrl?.isNotBlank() == true -> {
                                        AsyncImage(
                                            model = profileState.user!!.profileImageUrl!!,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile",
                                            tint = BrandYellow,
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                }
                            }

                            // Camera Icon Button
                            val pickMedia = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.PickVisualMedia()
                            ) { uri: Uri? ->
                                uri?.let { selectedUri ->
                                    settingsViewModel.setProfileImage(selectedUri)
                                }
                            }

                            FloatingActionButton(
                                onClick = {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(40.dp),
                                containerColor = BrandYellow,
                                contentColor = Color.Black
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Fotoğrafı Değiştir",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandYellow
                        )
                    }
                }
            }

            // Personal Information Section
            item(key = "personal_info") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Kişisel Bilgiler",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        // Name
                        OutlinedTextField(
                            value = settingsState.name,
                            onValueChange = { settingsViewModel.updateName(it) },
                            label = { Text("Ad") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandYellow,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Surname
                        OutlinedTextField(
                            value = settingsState.surname,
                            onValueChange = { settingsViewModel.updateSurname(it) },
                            label = { Text("Soyad") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandYellow,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Email (Read-only)
                        OutlinedTextField(
                            value = profileState.user?.email ?: "",
                            onValueChange = {},
                            label = { Text("E-posta") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color.White.copy(alpha = 0.3f),
                                disabledTextColor = Color.White.copy(alpha = 0.7f),
                                disabledLabelColor = Color.White.copy(alpha = 0.5f)
                            )
                        )

                        // Department
                        OutlinedTextField(
                            value = settingsState.department,
                            onValueChange = { settingsViewModel.updateDepartment(it) },
                            label = { Text("Bölüm") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandYellow,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Bio
                        OutlinedTextField(
                            value = settingsState.bio,
                            onValueChange = { settingsViewModel.updateBio(it) },
                            label = { Text("Biyografi") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandYellow,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }

            // Tag Selection Section
            item(key = "tag_selection") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Tag",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Profesyonel durumunuzu belirtin",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // Tag Options
                        val tags = listOf(
                            Triple("JOB", "İş arıyor", Color(0xFF258A00)),
                            Triple("INT", "Staj arıyor", Color(0xFF005888)),
                            Triple("TEAM", "Proje veya ekip arkadaşı arıyor", Color(0xFFFFC107)),
                            Triple("HIRING", "İşe alım yapıyor / ekip büyütüyor", Color(0xFFB03737)),
                            Triple("MENTOR", "Mentorluk veriyor", Color(0xFF4B0079))
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            tags.forEach { (tag, description, tagColor) ->
                                val isSelected = settingsState.tag == tag
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) tagColor.copy(alpha = 0.3f) 
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) tagColor else Color.White.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            settingsViewModel.updateTag(if (isSelected) null else tag)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Tag badge
                                                Box(
                                                    modifier = Modifier
                                                        .background(tagColor, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = tag,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Text(
                                                    text = description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                settingsViewModel.updateTag(if (isSelected) null else tag)
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = tagColor,
                                                unselectedColor = Color.White.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Skills Section
            item(key = "skills") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Yetenekler",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        // Skills Input
                        var newSkill by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newSkill,
                                onValueChange = { newSkill = it },
                                label = { Text("Yetenek ekle") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandYellow,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    if (newSkill.isNotBlank()) {
                                        settingsViewModel.addSkill(newSkill.trim())
                                        newSkill = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandYellow,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Ekle")
                            }
                        }

                        // Skills List
                        if (settingsState.skills.isNotEmpty()) {
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                settingsState.skills.forEach { skill ->
                                    Surface(
                                        color = BrandYellow.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = skill,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = BrandYellow
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { settingsViewModel.removeSkill(skill) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = BrandYellow,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            }
            
            // Save Button and Logout Button - Floating at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrandBackgroundDark,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Save Button
                        Button(
                            onClick = {
                                settingsViewModel.saveProfile()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            enabled = !settingsState.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandYellow,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            if (settingsState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Kaydet",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Logout Button
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Çıkış Yap",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Çıkış Yap",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

