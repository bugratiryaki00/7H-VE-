package com.example.proto7hive.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proto7hive.ui.home.PostCard
import com.example.proto7hive.ui.jobs.JobCard
import com.example.proto7hive.ui.connections.ConnectionCard
import com.example.proto7hive.ui.screens.Routes
import com.example.proto7hive.ui.theme.BrandYellow
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    navController: NavController? = null,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory())
) {
    var searchQuery by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()

    // Debounced search - kullanıcı yazmayı bitirdikten sonra arama yap
    LaunchedEffect(searchQuery) {
        delay(300) // 300ms bekle
        viewModel.search(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController?.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { 
                Text(
                    "Search",
                    color = BrandYellow.copy(alpha = 0.7f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = BrandYellow,
                cursorColor = BrandYellow,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = BrandYellow,
                unfocusedPlaceholderColor = BrandYellow,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            singleLine = true
        )

        // Search Results Area
        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter a search query to find users, posts, and jobs",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandYellow)
                    }
                }
                state.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.errorMessage}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                state.users.isEmpty() && state.posts.isEmpty() && state.jobs.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Users Section
                        if (state.users.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Users (${state.users.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(state.users) { user ->
                                UserSearchResultCard(
                                    user = user,
                                    onClick = {
                                        navController?.navigate(Routes.userProfile(user.id))
                                    }
                                )
                            }
                        }

                        // Posts Section
                        if (state.posts.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Posts (${state.posts.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = if (state.users.isNotEmpty()) 8.dp else 0.dp)
                                )
                            }
                            items(state.posts) { post ->
                                // Post için user bilgisi yok, bu yüzden null gönderiyoruz
                                // İsterseniz userId'ye göre user bilgisini yükleyebilirsiniz
                                PostCard(
                                    post = post,
                                    user = null,
                                    onCommentClick = {
                                        navController?.navigate(Routes.commentsPost(post.id))
                                    },
                                    onUserClick = {
                                        navController?.navigate(Routes.userProfile(post.userId))
                                    }
                                )
                            }
                        }

                        // Jobs Section
                        if (state.jobs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Jobs (${state.jobs.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = if (state.posts.isNotEmpty() || state.users.isNotEmpty()) 8.dp else 0.dp)
                                )
                            }
                            items(state.jobs) { job ->
                                JobCard(
                                    job = job,
                                    user = null, // Job için user bilgisi yok
                                    isSaved = false,
                                    onSaveClick = { },
                                    onRemoveClick = { },
                                    onClick = {
                                        navController?.navigate(Routes.userProfileWithJob(job.userId, job.id))
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

@Composable
private fun UserSearchResultCard(
    user: com.example.proto7hive.model.User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrandYellow.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl != null && user.profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = BrandYellow,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = listOfNotNull(user.name, user.surname).joinToString(" "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (user.department != null && user.department.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.department,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (user.bio != null && user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

