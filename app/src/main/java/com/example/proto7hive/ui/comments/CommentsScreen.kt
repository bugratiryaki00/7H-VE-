package com.example.proto7hive.ui.comments

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.data.FirestoreCommentRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow
import com.example.proto7hive.ui.home.getTimeAgo

@Composable
fun CommentsRoute(
    postId: String? = null,
    jobId: String? = null,
    onBack: () -> Unit
) {
    val viewModel: CommentsViewModel = viewModel(
        factory = CommentsViewModelFactory(postId = postId, jobId = jobId)
    )
    CommentsScreen(
        viewModel = viewModel,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    viewModel: CommentsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Yorumlar",
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBackgroundDark
                )
            )
        },
        containerColor = BrandBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Comments List
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandYellow)
                        }
                    }
                    state.comments.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz yorum yok\nİlk yorumu sen yap!",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.comments) { comment ->
                                CommentItem(
                                    comment = comment,
                                    user = state.users[comment.userId]
                                )
                            }
                        }
                    }
                }
            }

            // New Comment Input - Column içinde en altta
            Divider(color = Color.White.copy(alpha = 0.1f))
            Surface(
                color = BrandBackgroundDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.newCommentText,
                        onValueChange = { viewModel.updateCommentText(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Yorum yaz...",
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BrandYellow,
                            focusedContainerColor = Color(0xFF404040),
                            unfocusedContainerColor = Color(0xFF404040)
                        ),
                        keyboardOptions = KeyboardOptions(autoCorrect = true),
                        maxLines = 4,
                        enabled = !state.isPosting
                    )
                    
                    Button(
                        onClick = { viewModel.postComment() },
                        enabled = state.newCommentText.trim().isNotEmpty() && !state.isPosting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandYellow,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (state.isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Gönder")
                        }
                    }
                }
            }
        }
    }

    // Error Snackbar
    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Snackbar göster (ileride eklenebilir)
            viewModel.clearError()
        }
    }
}

@Composable
fun CommentItem(
    comment: com.example.proto7hive.model.Comment,
    user: com.example.proto7hive.model.User?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandYellow.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (user?.profileImageUrl != null && user.profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = BrandYellow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Comment Content
        Column(modifier = Modifier.weight(1f)) {
            // User Name & Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (user != null) {
                        listOfNotNull(user.name, user.surname).joinToString(" ")
                    } else {
                        "Kullanıcı"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                if (comment.timestamp > 0) {
                    Text(
                        text = getTimeAgo(comment.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Comment Text
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

