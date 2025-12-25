package com.example.proto7hive.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.R
import com.example.proto7hive.data.AuthRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Job
import com.example.proto7hive.ui.auth.AuthViewModel
import com.example.proto7hive.ui.auth.AuthViewModelFactory
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.home.PostCard
import com.example.proto7hive.ui.home.getTimeAgo
import com.example.proto7hive.ui.jobs.JobCard
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun ProfileRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository())),
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory()
    )
) {
    val profileState by profileViewModel.uiState.collectAsState()
    ProfileScreen(
        profileState = profileState,
        authViewModel = authViewModel,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToSettings = onNavigateToSettings,
        onRefresh = { profileViewModel.refresh() }
    )
}

@Composable
fun ProfileScreen(
    profileState: ProfileUiState,
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onRefresh: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = POSTS, 1 = WORKS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackgroundDark)
    ) {
        // Header with Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(72.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Search Bar
        SearchBar(
            onSearchClick = { /* TODO: Navigate to search screen */ },
            onNotificationClick = { /* TODO: Navigate to notifications */ }
        )

        // Content Area
        when {
            profileState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            }
            profileState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hata: ${profileState.errorMessage}",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRefresh) {
                        Text("Yeniden Dene", color = BrandYellow)
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Profile Header
                    ProfileHeader(
                        user = profileState.user,
                        postsCount = profileState.posts.count { it.postType == "post" },
                        worksCount = profileState.posts.count { it.postType == "work" } + profileState.jobs.size,
                        connectionsCount = profileState.connectionsCount,
                        onEditProfile = onNavigateToSettings
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
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                        // Profile Options when no posts
                                        ProfileOptions(
                                            onNavigateToOnboarding = onNavigateToOnboarding,
                                            onNavigateToSettings = onNavigateToSettings,
                                            onLogout = {
                                                authViewModel.signOut()
                                                onNavigateToOnboarding()
                                            }
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        items(postsOnly) { post ->
                                            PostCard(
                                                post = post,
                                                user = profileState.user
                                            )
                                        }
                                        
                                        // Profile Options at the end of posts
                                        item {
                                            Spacer(modifier = Modifier.height(24.dp))
                                            ProfileOptions(
                                                onNavigateToOnboarding = onNavigateToOnboarding,
                                                onLogout = {
                                                    authViewModel.signOut()
                                                    onNavigateToOnboarding()
                                                }
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
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                        // Profile Options when no works
                                        ProfileOptions(
                                            onNavigateToOnboarding = onNavigateToOnboarding,
                                            onNavigateToSettings = onNavigateToSettings,
                                            onLogout = {
                                                authViewModel.signOut()
                                                onNavigateToOnboarding()
                                            }
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp)
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
                                            JobCard(
                                                job = job,
                                                user = profileState.user,
                                                isSaved = false,
                                                onSaveClick = { /* Profile'da save/unsave işlemi yapılmaz */ },
                                                onRemoveClick = { /* Profile'da remove işlemi yapılmaz */ }
                                            )
                                        }
                                        
                                        // Profile Options at the end
                                        item {
                                            Spacer(modifier = Modifier.height(24.dp))
                                            ProfileOptions(
                                                onNavigateToOnboarding = onNavigateToOnboarding,
                                                onLogout = {
                                                    authViewModel.signOut()
                                                    onNavigateToOnboarding()
                                                }
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
    onEditProfile: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF212121)
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
                        .background(BrandBackgroundDark),
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
                    color = Color.White,
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

            // JOB Button (green) and Edit Profile Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* TODO: Navigate to job applications */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Green
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "JOB",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Edit Profile Button (pencil icon)
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

        Spacer(modifier = Modifier.height(12.dp))

        // Bio and Department below profile picture
        Column {
            // Bio
            if (user?.bio != null && user.bio.isNotBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // University/Department
            if (user?.department != null && user.department.isNotBlank()) {
                Text(
                    text = user.department,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
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
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
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
            containerColor = Color(0xFF212121)
        ),
        shape = RoundedCornerShape(12.dp)
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
                        color = if (selectedTab == 0) BrandYellow else Color.White.copy(alpha = 0.7f)
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
                        color = if (selectedTab == 1) BrandYellow else Color.White.copy(alpha = 0.7f)
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
                        .background(if (selectedTab == 0) BrandYellow else Color.White.copy(alpha = 0.3f))
                )
                // Right half (WORKS)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (selectedTab == 1) BrandYellow else Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun ProfileOptions(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                ProfileOptionItem(
                    icon = Icons.Default.Edit,
                    title = "Profili Düzenle",
                    subtitle = "Kişisel bilgilerinizi güncelleyin",
                    onClick = onNavigateToSettings
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Çıkış Yap",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Çıkış Yap",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = BrandYellow,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Arrow",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
