package com.example.proto7hive.ui.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.proto7hive.R
import com.example.proto7hive.ui.screens.Routes
import com.example.proto7hive.ui.theme.BrandYellow
import com.example.proto7hive.ui.components.SearchBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationRoute(
    navController: NavController? = null
) {
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory()
    )
    
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    
    NotificationScreen(
        viewModel = viewModel,
        navController = navController
    )
}

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    navController: NavController? = null
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("View all", "Mentions", "Followers", "Invites")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo (centered)
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(100.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        // Search Bar with Notification Icon
        SearchBar(
            modifier = Modifier.padding(top = 0.dp, bottom = 0.dp),
            onSearchClick = {
                navController?.navigate(Routes.SEARCH)
            },
            onNotificationClick = {
                // Notification icon'a tıklayınca geri çıkış
                navController?.popBackStack()
            },
            unreadCount = state.unreadCount,
            isInNotificationScreen = true
        )
        
        // Tabs - Kutu içinde, aktif olan sarı çerçeveli
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 0.5.dp,
                            color = if (isSelected) BrandYellow else Color.Black,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = Color.Transparent, // İç rengi her zaman şeffaf
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTabIndex = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        
        // Content
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            }
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hata: ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            else -> {
                val notificationsToShow = when (selectedTabIndex) {
                    0 -> state.allNotifications // View all
                    1 -> state.commentNotifications // Mentions (Comments)
                    2 -> state.followerNotifications // Followers
                    3 -> state.inviteNotifications // Invite
                    else -> emptyList()
                }
                
                if (notificationsToShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(bottom = 60.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(notificationsToShow) { notificationWithUser ->
                            NotificationItem(
                                notificationWithUser = notificationWithUser,
                                viewModel = viewModel,
                                selectedTabIndex = selectedTabIndex,
                                onClick = {
                                    // Mark as read
                                    if (!notificationWithUser.notification.isRead) {
                                        viewModel.markAsRead(notificationWithUser.notification.id)
                                    }
                                    
                                    // Navigate based on type
                                    when (notificationWithUser.notification.type) {
                                        "COMMENT" -> {
                                            // "liked your post" bildirimleri için HomeScreen'e git
                                            val message = notificationWithUser.notification.message.lowercase()
                                            if (message.contains("liked your post")) {
                                                // HomeScreen'e navigate et
                                                navController?.navigate(Routes.HOME) {
                                                    popUpTo(Routes.HOME) { inclusive = false }
                                                }
                                            } else {
                                                // Normal comment bildirimleri için comments sayfasına git
                                                notificationWithUser.notification.relatedId?.let { relatedId ->
                                                    if (notificationWithUser.notification.relatedType == "post") {
                                                        navController?.navigate(Routes.commentsPost(relatedId))
                                                    } else if (notificationWithUser.notification.relatedType == "job") {
                                                        navController?.navigate(Routes.commentsJob(relatedId))
                                                    }
                                                }
                                            }
                                        }
                                        "FOLLOW_REQUEST" -> {
                                            notificationWithUser.notification.fromUserId.let { userId ->
                                                navController?.navigate(Routes.userProfile(userId))
                                            }
                                        }
                                        "INVITE" -> {
                                            // İşverenin (fromUserId) profiline git
                                            notificationWithUser.notification.fromUserId.let { userId ->
                                                navController?.navigate(Routes.userProfile(userId))
                                            }
                                        }
                                    }
                                }
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notificationWithUser: NotificationWithUser,
    viewModel: NotificationViewModel,
    selectedTabIndex: Int,
    onClick: () -> Unit
) {
    val notification = notificationWithUser.notification
    val fromUser = notificationWithUser.fromUser
    val isFollowRequest = notification.type == "FOLLOW_REQUEST" && selectedTabIndex == 2 // Followers tab
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(BrandYellow.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (fromUser?.profileImageUrl != null && fromUser.profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = fromUser.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = BrandYellow,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name and Message
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = fromUser?.let { "${it.name} ${it.surname}" } ?: "Unknown User",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Accept/Reject buttons for FOLLOW_REQUEST in Followers tab
        if (isFollowRequest) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accept Button
                Button(
                    onClick = {
                        viewModel.acceptConnectionRequest(
                            notification.id,
                            notification.relatedId
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Accept",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Reject Button
                OutlinedButton(
                    onClick = {
                        viewModel.rejectConnectionRequest(
                            notification.id,
                            notification.relatedId
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Reject",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Unread indicator (for non-follow-request notifications)
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandYellow)
                )
            }
        }
    }
}

