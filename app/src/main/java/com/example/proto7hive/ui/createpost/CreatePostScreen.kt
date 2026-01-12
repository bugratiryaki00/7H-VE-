package com.example.proto7hive.ui.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar: X | Logo | Share Button - Üstte, minimal padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
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
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Logo (Center) - Sağa kaydırmak için offset ekle
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier
                    .height(100.dp)
                    .offset(x = 23.dp),
                contentScale = ContentScale.Fit
            )

            // Share Button
            val canShare = when (state.postType) {
                "work" -> {
                    if (state.isJobPosting == true) {
                        // Çalışan arıyor - title, company ve workType dolu olmalı
                        state.workTitle.isNotBlank() && 
                        state.workCompany.isNotBlank() && 
                        state.workType.isNotBlank() &&
                        state.selectedCollectionId != null
                    } else if (state.isJobPosting == false) {
                        // Personal Work/Portfolio - koleksiyon ve (image veya text) olmalı
                        state.selectedCollectionId != null && 
                        (state.imageUri != null || state.text.trim().isNotEmpty())
                    } else {
                        // Henüz seçim yapılmamış
                        false
                    }
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
                    disabledContentColor = MaterialTheme.colorScheme.onSurface
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
                    containerColor = if (state.postType == "post") BrandYellow else MaterialTheme.colorScheme.surface,
                    contentColor = if (state.postType == "post") Color.Black else MaterialTheme.colorScheme.onSurface
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
                    containerColor = if (state.postType == "work") BrandYellow else MaterialTheme.colorScheme.surface,
                    contentColor = if (state.postType == "work") Color.Black else MaterialTheme.colorScheme.onSurface
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
                        containerColor = MaterialTheme.colorScheme.surface
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
                        
                        // Image Icon Button launcher
                        val pickMedia = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickVisualMedia()
                        ) { uri: Uri? ->
                            uri?.let { selectedUri ->
                                viewModel.setSelectedImage(selectedUri)
                            }
                        }
                        
                        // Text Field with Image Button on top right
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            TextField(
                                value = state.text,
                                onValueChange = { viewModel.updateText(it) },
                                placeholder = {
                                    Text(
                                        text = "What do you want to share",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = BrandYellow,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text
                                ),
                                maxLines = 10,
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            
                            // Image Icon Button - Sağ üst köşede
                            IconButton(
                                onClick = {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 4.dp, top = 4.dp)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Add Image",
                                    tint = BrandYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

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
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.setSelectedImage(selectedUri)
        }
    }

    val workTypes = listOf("Full-time", "Part-time", "Remote", "Hybrid", "On-site")
    
    // Koleksiyon oluşturma dialog'u
    if (showCreateCollectionDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { collectionName ->
                viewModel.createCollection(collectionName) { collectionId ->
                    viewModel.updateSelectedCollection(collectionId)
                    showCreateCollectionDialog = false
                }
            }
        )
    }

    // Eğer isJobPosting henüz seçilmediyse, seçim ekranını göster
    if (state.isJobPosting == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Are you looking for employees?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Çalışan Arıyorum
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setIsJobPosting(true) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, BrandYellow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Looking for Employees",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fill in all information to share a job posting",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Kişisel İş/Portfolio
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setIsJobPosting(false) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Personal Work/Portfolio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Only image and collection selection required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tab Row - Sadece çalışan arıyorsa göster
        if (state.isJobPosting == true) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = BrandYellow
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Basic Information", color = if (selectedTab == 0) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Description & Photo", color = if (selectedTab == 1) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            if (state.isJobPosting == true) {
                // Çalışan arıyor - Mevcut tab yapısı
                when (selectedTab) {
                    0 -> WorkBasicInfoTab(
                        state = state,
                        viewModel = viewModel,
                        newSkill = newSkill,
                        onNewSkillChange = { newSkill = it },
                        workTypes = workTypes,
                        onShowCreateCollectionDialog = { showCreateCollectionDialog = true }
                    )
                    1 -> WorkDescriptionAndImageTab(
                        state = state,
                        viewModel = viewModel,
                        pickMedia = pickMedia
                    )
                }
            } else {
                // Kişisel iş - Sadece koleksiyon + görsel/metin
                WorkPortfolioContent(
                    state = state,
                    viewModel = viewModel,
                    pickMedia = pickMedia,
                    onShowCreateCollectionDialog = { showCreateCollectionDialog = true }
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
    workTypes: List<String>,
    onShowCreateCollectionDialog: () -> Unit
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
            label = { Text("Job Title *", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = BrandYellow,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            singleLine = true
        )

        // Company
        OutlinedTextField(
            value = state.workCompany,
            onValueChange = { viewModel.updateWorkCompany(it) },
            label = { Text("Company Name *", color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = BrandYellow,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            singleLine = true
        )

        // Location
        OutlinedTextField(
            value = state.workLocation,
            onValueChange = { viewModel.updateWorkLocation(it) },
            label = { Text("Location", color = MaterialTheme.colorScheme.onSurface) },
            placeholder = { Text("e.g: Istanbul, Remote, Hybrid/Kadıköy", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = BrandYellow,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                label = { Text("Work Type *", color = MaterialTheme.colorScheme.onSurface) },
                placeholder = { Text("Select", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                workTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            viewModel.updateWorkType(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Collection Dropdown
        var collectionExpanded by remember { mutableStateOf(false) }
        val selectedCollection = state.collections.find { it.id == state.selectedCollectionId }
        ExposedDropdownMenuBox(
            expanded = collectionExpanded,
            onExpandedChange = { collectionExpanded = !collectionExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCollection?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Collection *", color = MaterialTheme.colorScheme.onSurface) },
                placeholder = { Text("Select Collection", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = collectionExpanded)
                }
            )
            ExposedDropdownMenu(
                expanded = collectionExpanded,
                onDismissRequest = { collectionExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                // Yeni koleksiyon oluştur seçeneği (şimdilik boş - modal daha sonra eklenecek)
                DropdownMenuItem(
                    text = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = BrandYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Create New Collection",
                                color = BrandYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    onClick = {
                        collectionExpanded = false
                        onShowCreateCollectionDialog()
                    }
                )
                
                // Mevcut koleksiyonlar
                state.collections.forEach { collection ->
                    DropdownMenuItem(
                        text = { Text(collection.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            viewModel.updateSelectedCollection(collection.id)
                            collectionExpanded = false
                        }
                    )
                }
            }
        }

        // Required Skills
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Required Skills",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
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
                        label = { Text("Add Skill", color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandYellow,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                        Text("Add")
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
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeWorkSkill(skill) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurface,
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 80.dp)
        ) {
            // Description with Image Button on top right
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = state.workDescription,
                    onValueChange = { viewModel.updateWorkDescription(it) },
                    placeholder = {
                        Text(
                            text = "What do you want to share",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = BrandYellow,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    maxLines = 10,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                
                // Image Icon Button - Sağ üst köşede
                IconButton(
                    onClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = 4.dp) // Label olmadığı için padding azaltıldı
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add Image",
                        tint = BrandYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WorkPortfolioContent(
    state: CreatePostUiState,
    viewModel: CreatePostViewModel,
    pickMedia: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    onShowCreateCollectionDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Koleksiyon Seçimi
        Text(
            text = "Collection *",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        var collectionExpanded by remember { mutableStateOf(false) }
        val selectedCollection = state.collections.find { it.id == state.selectedCollectionId }
        ExposedDropdownMenuBox(
            expanded = collectionExpanded,
            onExpandedChange = { collectionExpanded = !collectionExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCollection?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Collection", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collectionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = BrandYellow,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = collectionExpanded,
                onDismissRequest = { collectionExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                state.collections.forEach { collection ->
                    DropdownMenuItem(
                        text = { Text(collection.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            viewModel.updateSelectedCollection(collection.id)
                            collectionExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BrandYellow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Collection", color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    onClick = {
                        collectionExpanded = false
                        onShowCreateCollectionDialog()
                    }
                )
            }
        }
        
        // Metin (Opsiyonel)
        Text(
            text = "Description (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        TextField(
            value = state.text,
            onValueChange = { viewModel.updateText(it) },
            placeholder = {
                Text(
                    text = "Add optional description",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = BrandYellow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5
        )
        
        // Görsel Ekleme
        Text(
            text = "Image (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (state.imageUri != null || state.imageUrl != null) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = state.imageUri ?: state.imageUrl,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { viewModel.removeImage() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Add Image",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Add Image",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Collection",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Collection Name", color = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandYellow,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = BrandYellow,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (collectionName.isNotBlank()) {
                        onCreate(collectionName.trim())
                    }
                },
                enabled = collectionName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
