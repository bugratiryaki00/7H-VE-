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
import coil.compose.AsyncImage
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.FirestoreConnectionRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow
import com.example.proto7hive.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun HomeFeedRoute(
    key: Any? = null // Dışarıdan değişince refresh tetiklenir
) {
    val viewModel: HomeFeedViewModel = viewModel(
        factory = HomeFeedViewModelFactory(
            FirestorePostRepository(),
            FirestoreConnectionRepository(),
            FirestoreUserRepository()
        )
    )
    
    // Key değiştiğinde (post oluşturulduğunda) refresh et
    LaunchedEffect(key) {
        viewModel.refresh()
    }
    
    HomeFeedScreen(viewModel = viewModel)
}

@Composable
fun HomeFeedScreen(
    viewModel: HomeFeedViewModel
) {
    val state by viewModel.uiState.collectAsState()
    
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
        
        // Content Area - Post Feed
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            }
            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                    modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.posts) { post ->
                        val user = state.users[post.userId]
                        PostCard(
                            post = post,
                            user = user
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
    user: com.example.proto7hive.model.User?
) {
    var isLiked by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header (Profile Picture + Name + Job + Time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                        text = user?.name ?: "Kullanıcı",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Job/Department
                    if (user?.department != null && user.department.isNotBlank()) {
                        Text(
                            text = user.department,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    } else if (user?.bio != null && user.bio.isNotBlank()) {
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Time ago
                    if (post.timestamp > 0) {
                        Text(
                            text = getTimeAgo(post.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Divider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Post Text
            Text(
                text = post.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
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
                    modifier = Modifier.clickable { isLiked = !isLiked },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLiked) BrandYellow else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Beğen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLiked) BrandYellow else Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Comment Button
                Row(
                    modifier = Modifier.clickable { /* TODO: Navigate to comments */ },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Yorum Yap",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yorum Yap",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
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

