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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        profileViewModel = profileViewModel,
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
        profileViewModel = profileViewModel,
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
    profileViewModel: ProfileViewModel,
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
        // Top Bar: Back Button (if needed) | Logo | Settings/Notification
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button (only for other users' profiles)
            if (onNavigateBack != {} && !isOwnProfile) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(80.dp),
                contentScale = ContentScale.Fit
            )

            // Settings or Notification Icon
            Row {
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (isOwnProfile) {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Search Bar
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
                        text = "Error: ${profileState.errorMessage}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRefresh) {
                        Text("Retry", color = BrandYellow)
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
                    when (selectedTab) {
                        0 -> {
                            // POSTS Tab - postType="post" olan postları göster
                            val postsOnly = profileState.posts.filter { it.postType == "post" }
                            val hasPosts = postsOnly.isNotEmpty()
                            
                            if (!hasPosts) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No posts yet",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
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
                            // WORKS Tab - Önce koleksiyonlar, sonra seçilen koleksiyondaki work'ler
                            if (profileState.selectedCollectionId == null) {
                                // Koleksiyon listesi göster (2 sütunlu grid)
                                if (profileState.collections.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No collections yet",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 60.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(profileState.collections) { collection ->
                                            CollectionGridCard(
                                                collection = collection,
                                                onClick = {
                                                    profileViewModel.selectCollection(collection.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Seçilen koleksiyondaki work'leri göster
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Back button
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { profileViewModel.selectCollection(null) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Back",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = profileState.collections.find { it.id == profileState.selectedCollectionId }?.name ?: "Collection",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    
                                    // Works list - PostCard gibi göster
                                    if (profileState.selectedCollectionPosts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No works in this collection",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(bottom = 60.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(profileState.selectedCollectionPosts) { post ->
                                                PostCard(
                                                    post = post,
                                                    user = profileState.user
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
                if (user?.profileImageUrl != null && user.profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(3.dp, BrandYellow, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(BrandYellow.copy(alpha = 0.3f))
                            .border(3.dp, BrandYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = BrandYellow,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // Name
                Text(
                    text = "${user?.name ?: ""} ${user?.surname ?: ""}".trim(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Statistics
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticItem(count = "$postsCount", label = "Posts")
                    StatisticItem(count = "$worksCount", label = "Works")
                    StatisticItem(count = "$connectionsCount+", label = "Bees")
                }

                // User Type & Department
                if (user?.userType != null && user.userType.isNotBlank()) {
                    Text(
                        text = "${user.userType}${if (user.department != null && user.department.isNotBlank()) " • ${user.department}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Department abbreviation
                if (user?.department != null && user.department.isNotBlank()) {
                    Text(
                        text = user.department.split(" ").map { it.firstOrNull()?.uppercase() ?: "" }.joinToString(""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Right side: Tag and Edit Icon
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
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

                // Edit Icon (only for own profile)
                if (isOwnProfile) {
                    IconButton(
                        onClick = onEditProfile,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = BrandYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Connection Button (only for other users' profiles)
        if (!isOwnProfile && onAddConnection != null && user != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val context = LocalContext.current
                
                // Connect/Follow Button
                val (buttonText, buttonColor, isPending) = when {
                    isConnected -> Triple("Connected", BrandYellow, false)
                    connectionRequestStatus == "pending" -> Triple("Pending", MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), true)
                    else -> Triple("Follow", BrandYellow, false)
                }
                
                if (isPending || !isConnected) {
                    Button(
                        onClick = {
                            if (!isPending && !isConnected) {
                                onAddConnection(user.id)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = if (isPending) MaterialTheme.colorScheme.onSurface else Color.Black
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

@Composable
fun CollectionGridCard(
    collection: Collection,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Collection image or default
            if (collection.thumbnailUrl != null && collection.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = collection.thumbnailUrl,
                    contentDescription = collection.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_collection_default),
                        contentDescription = "Default Collection",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Collection name overlay (bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
