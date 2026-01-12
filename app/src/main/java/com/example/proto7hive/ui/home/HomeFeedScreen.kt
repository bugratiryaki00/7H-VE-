package com.example.proto7hive.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.screens.Routes
import com.example.proto7hive.ui.theme.BrandYellow
import com.example.proto7hive.ui.notifications.NotificationViewModel
import com.example.proto7hive.ui.notifications.NotificationViewModelFactory
import com.example.proto7hive.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun HomeFeedRoute(
    navController: NavController? = null
) {
    val viewModel: HomeFeedViewModel = viewModel(
        factory = HomeFeedViewModelFactory(
            FirestorePostRepository(),
            FirestoreConnectionRepository(),
            FirestoreUserRepository()
        )
    )
    
    // Notification ViewModel for unread count
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory()
    )
    
    // İlk yüklemede refresh et
    LaunchedEffect(Unit) {
        viewModel.refresh()
        notificationViewModel.refresh()
    }
    
    HomeFeedScreen(
        viewModel = viewModel,
        notificationViewModel = notificationViewModel,
        navController = navController
    )
}

@Composable
fun HomeFeedScreen(
    viewModel: HomeFeedViewModel,
    notificationViewModel: NotificationViewModel,
    navController: NavController? = null
) {
    val state by viewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Logo - Üstte, minimal padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(100.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        // Search Bar - Logo'nun tam altında, arada boşluk yok
        SearchBar(
            modifier = Modifier.padding(top = 0.dp, bottom = 0.dp),
            onSearchClick = {
                navController?.navigate(com.example.proto7hive.ui.screens.Routes.SEARCH)
            },
            onNotificationClick = {
                navController?.navigate(com.example.proto7hive.ui.screens.Routes.NOTIFICATIONS)
            },
            unreadCount = notificationState.unreadCount,
            isInNotificationScreen = false
        )
        
        // Content Area - Post Feed - Kalan tüm alanı kaplar, navbar'ın üstüne kadar
        when {
            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hata: ${state.errorMessage}",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text("Yeniden Dene", color = BrandYellow)
                    }
                }
            }
            state.posts.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz paylaşım yok",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.posts) { post ->
                        val user = state.users[post.userId]
                        PostCard(
                            post = post,
                            user = user,
                            viewModel = viewModel,
                            onCommentClick = {
                                if (navController != null && post.id.isNotEmpty()) {
                                    navController.navigate(Routes.commentsPost(post.id))
                                }
                            },
                            onUserClick = {
                                if (navController != null && post.userId.isNotEmpty()) {
                                    navController.navigate(Routes.userProfile(post.userId))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: com.example.proto7hive.model.Post,
    user: com.example.proto7hive.model.User?,
    viewModel: HomeFeedViewModel? = null,
    onCommentClick: () -> Unit = {},
    onUserClick: () -> Unit = {}
) {
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    val isLiked = currentUserId != null && post.likes.contains(currentUserId)
    val likeCount = post.likes.size
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header (Profile Picture + Name + Job + Time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable(onClick = onUserClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandYellow.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.profileImageUrl != null && user.profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = BrandYellow,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name, Job, Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (user != null) {
                            listOfNotNull(user.name, user.surname).joinToString(" ")
                        } else {
                            "Kullanıcı"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Job/Department
                    if (user?.department != null && user.department.isNotBlank()) {
                        Text(
                            text = user.department,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else if (user?.bio != null && user.bio.isNotBlank()) {
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Time ago
                    if (post.timestamp > 0) {
                        Text(
                            text = getTimeAgo(post.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Post Text
            Text(
                text = post.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = if (post.imageUrl != null) 12.dp else 0.dp)
            )
            
            // Post Image
            post.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Action Buttons (Like & Comment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Like Button
                Row(
                    modifier = Modifier.clickable(enabled = viewModel != null) { 
                        viewModel?.let {
                            if (isLiked) {
                                it.unlikePost(post.id)
                            } else {
                                it.likePost(post.id)
                            }
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLiked) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (likeCount > 0) "$likeCount Beğeni" else "Beğen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLiked) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Comment Button
                Row(
                    modifier = Modifier.clickable(onClick = onCommentClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Yorum Yap",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yorum Yap",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Helper function to get "time ago" format
fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days gün önce"
        hours > 0 -> "$hours saat önce"
        minutes > 0 -> "$minutes dakika önce"
        else -> "Az önce"
    }
}

