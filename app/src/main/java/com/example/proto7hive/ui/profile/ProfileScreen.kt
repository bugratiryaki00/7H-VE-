package com.example.proto7hive.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.proto7hive.model.Project
import com.example.proto7hive.ui.auth.AuthViewModel
import com.example.proto7hive.ui.auth.AuthViewModelFactory
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.home.PostCard
import com.example.proto7hive.ui.home.getTimeAgo
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun ProfileRoute(
    onNavigateToOnboarding: () -> Unit,
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
        onRefresh = { profileViewModel.refresh() }
    )
}

@Composable
fun ProfileScreen(
    profileState: ProfileUiState,
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
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
                modifier = Modifier.height(32.dp),
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
                        postsCount = profileState.posts.size,
                        worksCount = profileState.projects.size,
                        connectionsCount = profileState.connectionsCount
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
                                // POSTS Tab
                                if (profileState.posts.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Henüz post paylaşılmamış",
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        items(profileState.posts) { post ->
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
                                // WORKS Tab
                                if (profileState.projects.isEmpty()) {
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
                                                text = "Henüz proje eklenmemiş",
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                        // Profile Options when no projects
                                        ProfileOptions(
                                            onNavigateToOnboarding = onNavigateToOnboarding,
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
                                        items(profileState.projects) { project ->
                                            ProjectCard(project = project)
                                        }
                                        
                                        // Profile Options at the end of projects
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
    connectionsCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BrandYellow.copy(alpha = 0.3f)),
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

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user?.name ?: "Kullanıcı",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // University/Department
                if (user?.department != null && user.department.isNotBlank()) {
                    Text(
                        text = user.department,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // JOB Button (green)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StatisticItem(label = "$postsCount Posts")
            StatisticItem(label = "$worksCount Works")
            StatisticItem(label = "$connectionsCount+ Bees")
        }
    }
}

@Composable
fun StatisticItem(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // POSTS Tab
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "POSTS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) BrandYellow else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // WORKS Tab
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "WORKS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) BrandYellow else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Project Image (if exists)
            project.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Project Title
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Project Description
            if (project.description.isNotBlank()) {
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Tags
            if (project.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    project.tags.take(3).forEach { tag ->
                        Surface(
                            color = BrandYellow.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandYellow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptions(
    onNavigateToOnboarding: () -> Unit,
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
                    onClick = { /* TODO: Navigate to edit profile */ }
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                ProfileOptionItem(
                    icon = Icons.Default.WorkspacePremium,
                    title = "Rozetlerim",
                    subtitle = "Kazandığınız rozetleri görün",
                    onClick = { /* TODO: Navigate to badges */ }
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                ProfileOptionItem(
                    icon = Icons.Default.Settings,
                    title = "Ayarlar",
                    subtitle = "Uygulama ayarları",
                    onClick = { /* TODO: Navigate to settings */ }
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                ProfileOptionItem(
                    icon = Icons.Default.Help,
                    title = "Yardım",
                    subtitle = "Sık sorulan sorular",
                    onClick = { /* TODO: Navigate to help */ }
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
private fun ProfileOptionItem(
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
