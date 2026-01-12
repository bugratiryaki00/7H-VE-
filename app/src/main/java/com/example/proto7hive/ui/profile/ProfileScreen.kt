package com.example.proto7hive.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.R
import android.content.Intent
import android.net.Uri
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.Collection
import com.example.proto7hive.ui.auth.AuthViewModel
import com.example.proto7hive.ui.auth.AuthViewModelFactory
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.home.PostCard
import com.example.proto7hive.ui.home.getTimeAgo
import com.example.proto7hive.ui.jobs.JobCard
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun ProfileRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    refreshKey: Int = 0, // Key değiştiğinde ViewModel refresh edilir
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository())),
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory() // userId null, current user için
    )
) {
    val profileState by profileViewModel.uiState.collectAsState()
    
    // Key değiştiğinde (Settings'ten dönüldüğünde) refresh yap
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) {
            profileViewModel.refresh()
        }
    }
    
    ProfileScreen(
        profileState = profileState,
        authViewModel = authViewModel,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToNotifications = onNavigateToNotifications,
        onRefresh = { profileViewModel.refresh() },
        isOwnProfile = true,
        onAddConnection = null // Kendi profilinde connect butonu yok
    )
}

@Composable
fun UserProfileRoute(
    userId: String,
    selectedJobId: String? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository())),
    profileViewModel: ProfileViewModel = viewModel(
factory = ProfileViewModelFactory(userId = userId)
    )
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val currentUserId = authState.user?.uid
    
    ProfileScreen(
        profileState = profileState,
        authViewModel = authViewModel,
        onNavigateToOnboarding = { },
        onNavigateToSettings = { }, // Başka kullanıcının profilinde settings yok
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToNotifications = onNavigateToNotifications,
        onRefresh = { profileViewModel.refresh() },
        isOwnProfile = currentUserId == userId,
        onNavigateBack = onNavigateBack,
        initialTab = if (selectedJobId != null) 1 else 0, // Works tab if jobId provided
        selectedJobId = selectedJobId,
        onAddConnection = { targetUserId ->
            profileViewModel.sendConnectionRequest(targetUserId)
        }
    )
}

@Composable
fun ProfileScreen(
    profileState: ProfileUiState,
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onRefresh: () -> Unit,
    isOwnProfile: Boolean = true,
    onNavigateBack: () -> Unit = {},
    initialTab: Int = 0, // 0 = POSTS, 1 = WORKS
    selectedJobId: String? = null, // Highlight this job if provided
    onAddConnection: ((String) -> Unit)? = null // Connect butonu için
) {
    val authState by authViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(initialTab) } // 0 = POSTS, 1 = WORKS
    val isConnected = profileState.isConnected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Logo and Back Button (if not own profile) - Üstte, minimal padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button (only for other users' profiles)
            if (!isOwnProfile) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Spacer to center logo when no back button
            }
            
            // Logo (centered)
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(80.dp),
                contentScale = ContentScale.Fit
            )
            
            // Right side spacer for symmetry
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Search Bar - Logo'nun tam altında, arada boşluk yok
        SearchBar(
            modifier = Modifier.padding(top = 0.dp, bottom = 0.dp),
            onSearchClick = onNavigateToSearch,
            onNotificationClick = onNavigateToNotifications
        )

        // Content Area
        when {
            profileState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hata: ${profileState.errorMessage}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRefresh) {
                        Text("Yeniden Dene", color = BrandYellow)
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.weight(1f)) {
                    // Profile Header
                    val onEditProfileClick: () -> Unit = if (isOwnProfile) {
                        onNavigateToSettings
                    } else {
                        {}
                    }
                    ProfileHeader(
                        user = profileState.user,
                        postsCount = profileState.posts.count { it.postType == "post" },
                        worksCount = profileState.collections.size,
                        connectionsCount = profileState.connectionsCount,
                        onEditProfile = onEditProfileClick,
                        isOwnProfile = isOwnProfile,
                        isConnected = isConnected,
                        connectionRequestStatus = profileState.connectionRequestStatus,
                        onAddConnection = onAddConnection
                    )

                    // Tabs (POSTS / WORKS)
                    ProfileTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    // Content based on selected tab
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> {
                                // POSTS Tab - Sadece postType="post" olanları göster
                                val postsOnly = profileState.posts.filter { it.postType == "post" }
                                if (postsOnly.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Henüz post paylaşılmamış",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 60.dp)
                                    ) {
                                        items(postsOnly) { post ->
                                            PostCard(
                                                post = post,
                                                user = profileState.user
                                            )
                                        }
                                        
                                    }
                                }
                            }
                            1 -> {
                                // WORKS Tab - postType="work" olan postları ve jobs'ları göster
                                val worksOnly = profileState.posts.filter { it.postType == "work" }
                                val hasWorks = worksOnly.isNotEmpty() || profileState.jobs.isNotEmpty()
                                
                                if (!hasWorks) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Henüz work paylaşılmamış",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 60.dp)
                                    ) {
                                        // Work type postları göster
                                        items(worksOnly) { post ->
                                            PostCard(
                                                post = post,
                                                user = profileState.user
                                            )
                                        }
                                        
                                        // Jobs'ları göster
                                        items(profileState.jobs) { job ->
                                            val isHighlighted = selectedJobId == job.id
                                            JobCard(
                                                job = job,
                                                user = profileState.user,
                                                isSaved = false,
                                                onSaveClick = { /* Profile'da save/unsave işlemi yapılmaz */ },
                                                onRemoveClick = { /* Profile'da remove işlemi yapılmaz */ },
                                                isHighlighted = isHighlighted
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
}

@Composable
fun ProfileHeader(
    user: com.example.proto7hive.model.User?,
    postsCount: Int,
    worksCount: Int,
    connectionsCount: Int,
    onEditProfile: () -> Unit = {},
    isOwnProfile: Boolean = true,
    isConnected: Boolean = false,
    connectionRequestStatus: String? = null, // null, "pending", "sent"
    onAddConnection: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Profile Picture with yellow border
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Outer circle with border (80dp)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(
                            width = 2.dp,
                            color = BrandYellow,
                            shape = CircleShape
                        )
                )
                
                // Inner profile image (76dp to account for border)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.profileImageUrl != null && user.profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = null,
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
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Statistics on the right
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = if (user != null) {
                        listOfNotNull(user.name, user.surname).joinToString(" ")
                    } else {
                        "Kullanıcı"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Statistics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticItem(count = postsCount.toString(), label = "Posts")
                    StatisticItem(count = worksCount.toString(), label = "Works")
                    StatisticItem(count = "$connectionsCount+", label = "Bees")
                }
            }

            // User Type Tag and Edit Profile Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tag (sadece JOB, INT, TEAM, HIRING, MENTOR göster) - badges'den al, userType'dan değil
                val badgeValue = user?.badges?.firstOrNull()
                val tagValue = badgeValue?.uppercase()?.trim() ?: ""
                val (tagText, tagColor) = when (tagValue) {
                    "JOB" -> Pair("JOB", Color(0xFF258A00)) // Yeşil - İş arıyor
                    "INT" -> Pair("INT", Color(0xFF005888)) // Mavi - Staj arıyor
                    "TEAM" -> Pair("TEAM", Color(0xFFFFC107)) // Sarı - Proje veya ekip arkadaşı arıyor
                    "HIRING" -> Pair("HIRING", Color(0xFFB03737)) // Kırmızı - İşe alım yapıyor
                    "MENTOR" -> Pair("MENTOR", Color(0xFF4B0079)) // Mor - Mentorluk veriyor
                    else -> Pair(null, null) // Student, Academician, Staff, Graduate gibi değerler gösterilmez
                }
                
                // Tag göster (badge varsa ve geçerli bir tag ise)
                if (!tagText.isNullOrBlank() && tagColor != null) {
                    Button(
                        onClick = { /* User type tag, no action */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tagColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tagText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Edit Profile Button (pencil icon) - sadece kendi profilinde göster
                if (isOwnProfile) {
                    IconButton(
                        onClick = onEditProfile,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Profili Düzenle",
                            tint = BrandYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bio and Department below profile picture
        Column {
            // Bio
            if (user?.bio != null && user.bio.isNotBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // University/Department
            if (user?.department != null && user.department.isNotBlank()) {
                Text(
                    text = user.department,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = if (!isOwnProfile) 8.dp else 0.dp)
                )
            }
            
            // Follow and Email Buttons (sadece başka kullanıcının profilinde, sol alt köşede)
            if (!isOwnProfile && onAddConnection != null && user?.id != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val context = LocalContext.current
                val isPending = connectionRequestStatus == "pending"
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Follow/Connected Button
                    if (isConnected) {
                        // Connected state - Outlined button style
                        OutlinedButton(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF353535),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = Color(0xFF353535),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Follow or Pending state
                        Button(
                            onClick = { 
                                if (!isPending) {
                                    onAddConnection(user.id)
                                }
                            },
                            enabled = !isPending,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPending) MaterialTheme.colorScheme.surface else BrandYellow,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                contentColor = if (isPending) MaterialTheme.colorScheme.onSurface else Color.Black,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = if (isPending) "Pending" else "Follow",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    // Email Button
                    if (user.email != null && user.email.isNotBlank()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${user.email}")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                                } catch (e: Exception) {
                                    // Email app yoksa hiçbir şey yapma
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandYellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "E-Mail",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun StatisticItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // POSTS Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(0) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POSTS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // WORKS Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(1) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WORKS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Combined underline - left yellow when POSTS selected, right yellow when WORKS selected
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left half (POSTS)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (selectedTab == 0) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                )
                // Right half (WORKS)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (selectedTab == 1) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                )
            }
        }
    }
}

