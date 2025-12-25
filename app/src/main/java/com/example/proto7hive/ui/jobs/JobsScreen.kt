package com.example.proto7hive.ui.jobs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.R
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.User
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.theme.BrandBackgroundDark
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun JobsRoute() {
    val viewModel: JobsViewModel = viewModel(
        factory = JobsViewModelFactory()
    )
    JobsScreen(viewModel = viewModel)
}

@Composable
fun JobsScreen(
    viewModel: JobsViewModel
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
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Job interests for you Section
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Job interests for you!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                            )
                            Text(
                                text = "Take a look at these job opportunities that we chose for you.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )

                            if (state.recommendedJobs.isEmpty()) {
                                Text(
                                    text = "Henüz önerilen iş yok",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                                )
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    state.recommendedJobs.forEach { job ->
                                        val user = state.users[job.userId]
                                        JobCard(
                                            job = job,
                                            user = user,
                                            isSaved = false,
                                            onSaveClick = { viewModel.saveJob(job.id) },
                                            onRemoveClick = { viewModel.removeRecommendedJob(job.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Saved jobs Section
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Saved jobs",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )

                            if (state.savedJobs.isEmpty()) {
                                Text(
                                    text = "Henüz kaydedilmiş iş yok",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                                )
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    state.savedJobs.forEach { job ->
                                        val user = state.users[job.userId]
                                        JobCard(
                                            job = job,
                                            user = user,
                                            isSaved = true,
                                            onSaveClick = { viewModel.unsaveJob(job.id) },
                                            onRemoveClick = { viewModel.unsaveJob(job.id) }
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
fun JobCard(
    job: Job,
    user: User? = null,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF212121)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Logo/Icon (Left)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrandYellow.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (job.imageUrl != null && job.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = job.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Company initials or icon - larger text (eğer company varsa)
                    if (job.company.isNotBlank()) {
                        Text(
                            text = job.company.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandYellow,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Work post için basit icon
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Work",
                            tint = BrandYellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Job Info (Middle)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Job Title - Company
                Text(
                    text = if (job.company.isNotBlank()) "${job.title} – ${job.company}" else job.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // User name (paylaşan kullanıcı)
                if (user != null) {
                    val fullName = listOfNotNull(user.name, user.surname).joinToString(" ")
                    if (fullName.isNotBlank()) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandYellow.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Location and Work Type
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (job.location.isNotBlank()) {
                        Text(
                            text = job.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    if (job.workType.isNotBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = job.workType,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Actions (Right)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save/Unsave Button
                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Kaydı kaldır" else "Kaydet",
                        tint = if (isSaved) BrandYellow else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Remove/Dismiss Button (X icon)
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kaldır",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
