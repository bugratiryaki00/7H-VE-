package com.example.proto7hive.ui.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proto7hive.R
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun CreatePostRoute(
    navController: NavController,
    onPostCreated: () -> Unit = { navController.popBackStack() }
) {
    val viewModel: CreatePostViewModel = viewModel(
        factory = CreatePostViewModelFactory()
    )
    CreatePostScreen(
        viewModel = viewModel,
        onClose = { navController.popBackStack() },
        onPostCreated = onPostCreated
    )
}

@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onClose: () -> Unit,
    onPostCreated: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Post başarıyla oluşturulunca callback çağır
    LaunchedEffect(state.postSuccess) {
        if (state.postSuccess) {
            onPostCreated()
            viewModel.resetState()
        }
    }

    // Error mesajını Snackbar ile göster
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackgroundDark)
    ) {
        // Top Bar: X | Logo | Share Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button (X)
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Logo (Center)
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(72.dp),
                contentScale = ContentScale.Fit
            )

            // Share Button
            val canShare = when (state.postType) {
                "work" -> {
                    // Work için: title, company ve workType dolu olmalı
                    state.workTitle.isNotBlank() && 
                    state.workCompany.isNotBlank() && 
                    state.workType.isNotBlank()
                }
                else -> {
                    // Post için: text veya image olmalı
                    state.text.trim().isNotEmpty() || state.imageUri != null
                }
            }
            
            Button(
                onClick = {
                    viewModel.sharePost(onPostCreated)
                },
                enabled = canShare && !state.isPosting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                if (state.isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "share",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Post Type Selection (Post/Work)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Post Button
                Button(
                    onClick = { viewModel.setPostType("post") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.postType == "post") BrandYellow else Color(0xFF404040),
                        contentColor = if (state.postType == "post") Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Post",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                
                // Work Button
                Button(
                    onClick = { viewModel.setPostType("work") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.postType == "work") BrandYellow else Color(0xFF404040),
                        contentColor = if (state.postType == "work") Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Work",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

            // Content based on post type
            if (state.postType == "work") {
                // Work Form
                WorkFormContent(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                // Post Text Input Area with Image Preview inside
                val postScrollState = rememberScrollState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF404040) // Gri renk tonu
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(postScrollState)
                            .padding(bottom = 80.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Text Field
                        TextField(
                            value = state.text,
                            onValueChange = { viewModel.updateText(it) },
                            placeholder = {
                                Text(
                                    text = "What do you want to share",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .heightIn(min = 100.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = BrandYellow,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color(0xFF404040),
                                    unfocusedContainerColor = Color(0xFF404040)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text
                                ),
                                maxLines = 10,
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Image Icon Button - Alt kısımda görsel butonu
                        val pickMedia = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickVisualMedia()
                        ) { uri: Uri? ->
                            uri?.let { selectedUri ->
                                viewModel.setSelectedImage(selectedUri)
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fotoğraf Ekle",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Add Image",
                                    tint = BrandYellow,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Selected Image Preview - Card içinde alt kısımda
                        state.imageUri?.let { imageUri ->
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Remove button
                                IconButton(
                                    onClick = { viewModel.removeImage() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Image",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(20.dp)
                                        )
                                    }
                                }
                                
                                // Upload progress indicator
                                if (state.isUploadingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(48.dp),
                                        color = BrandYellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WorkFormContent(
    state: CreatePostUiState,
    viewModel: CreatePostViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Temel Bilgiler, 1 = Açıklama & Fotoğraf
    var newSkill by remember { mutableStateOf("") }
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.setSelectedImage(selectedUri)
        }
    }

    val workTypes = listOf("Full-time", "Part-time", "Remote", "Hybrid", "On-site")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BrandBackgroundDark,
            contentColor = BrandYellow
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Temel Bilgiler", color = if (selectedTab == 0) BrandYellow else Color.White.copy(alpha = 0.7f)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Açıklama & Fotoğraf", color = if (selectedTab == 1) BrandYellow else Color.White.copy(alpha = 0.7f)) }
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> WorkBasicInfoTab(
                    state = state,
                    viewModel = viewModel,
                    newSkill = newSkill,
                    onNewSkillChange = { newSkill = it },
                    workTypes = workTypes
                )
                1 -> WorkDescriptionAndImageTab(
                    state = state,
                    viewModel = viewModel,
                    pickMedia = pickMedia
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WorkBasicInfoTab(
    state: CreatePostUiState,
    viewModel: CreatePostViewModel,
    newSkill: String,
    onNewSkillChange: (String) -> Unit,
    workTypes: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        OutlinedTextField(
            value = state.workTitle,
            onValueChange = { viewModel.updateWorkTitle(it) },
            label = { Text("İş Başlığı *", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = BrandYellow,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),
            singleLine = true
        )

        // Company
        OutlinedTextField(
            value = state.workCompany,
            onValueChange = { viewModel.updateWorkCompany(it) },
            label = { Text("Şirket Adı *", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = BrandYellow,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),
            singleLine = true
        )

        // Location
        OutlinedTextField(
            value = state.workLocation,
            onValueChange = { viewModel.updateWorkLocation(it) },
            label = { Text("Lokasyon", color = Color.White) },
            placeholder = { Text("Örn: Istanbul, Remote, Hybrid/Kadıköy", color = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = BrandYellow,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),
            singleLine = true
        )

        // Work Type Dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.workType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Çalışma Tipi *", color = Color.White) },
                placeholder = { Text("Seçiniz", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF2A2A2A))
            ) {
                workTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type, color = Color.White) },
                        onClick = {
                            viewModel.updateWorkType(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Required Skills
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Gerekli Yetenekler",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Skill Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newSkill,
                        onValueChange = onNewSkillChange,
                        label = { Text("Yetenek ekle", color = Color.White) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        )
                    )
                    Button(
                        onClick = {
                            if (newSkill.isNotBlank()) {
                                viewModel.addWorkSkill(newSkill)
                                onNewSkillChange("")
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
                if (state.workRequiredSkills.isNotEmpty()) {
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.workRequiredSkills.forEach { skill ->
                            Surface(
                                color = BrandYellow.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = skill,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeWorkSkill(skill) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Kaldır",
                                            tint = Color.White,
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

@Composable
private fun WorkDescriptionAndImageTab(
    state: CreatePostUiState,
    viewModel: CreatePostViewModel,
    pickMedia: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>
) {
    val scrollState = rememberScrollState()
    
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 80.dp)
        ) {
            // Description
            OutlinedTextField(
                value = state.workDescription,
                onValueChange = { viewModel.updateWorkDescription(it) },
                label = { Text("Açıklama", color = Color.White) },
                placeholder = { Text("İş hakkında detaylı bilgi", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                ),
                maxLines = 10,
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Image Upload Section - Post'taki gibi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Şirket/Logo Görseli",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add Image",
                        tint = BrandYellow,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Selected Image Preview
            state.imageUri?.let { imageUri ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Remove button
                    IconButton(
                        onClick = { viewModel.removeImage() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                    
                    // Upload progress indicator
                    if (state.isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp),
                            color = BrandYellow
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
