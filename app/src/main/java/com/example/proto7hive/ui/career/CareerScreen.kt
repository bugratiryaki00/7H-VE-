package com.example.proto7hive.ui.career

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proto7hive.R
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.JobApplication
import com.example.proto7hive.model.User
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.screens.Routes
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun CareerRoute(
    navController: NavController? = null
) {
    val viewModel: CareerViewModel = viewModel(
        factory = CareerViewModelFactory()
    )
    CareerScreen(
        viewModel = viewModel,
        navController = navController
    )
}

@Composable
fun CareerScreen(
    viewModel: CareerViewModel,
    navController: NavController? = null
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Find Jobs, 1 = My Postings

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
            Image(
                painter = painterResource(id = R.drawable.ic_logo_7hive),
                contentDescription = "7HIVE Logo",
                modifier = Modifier.height(80.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Search Bar
        SearchBar(
            modifier = Modifier.padding(top = 0.dp, bottom = 0.dp),
            onSearchClick = {
                navController?.navigate(Routes.SEARCH)
            },
            onNotificationClick = {
                navController?.navigate(Routes.NOTIFICATIONS)
            }
        )

        // Tabs (Find Jobs / My Postings)
        CareerTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Content based on selected tab
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // Find Jobs Tab
                    FindJobsTab(
                        state = state,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                1 -> {
                    // My Postings Tab
                    MyPostingsTab(
                        state = state,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun CareerTabs(
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
                // Find Jobs Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(0) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Find Jobs",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // My Postings Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(1) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "My Postings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Combined underline
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left half (Find Jobs)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (selectedTab == 0) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                )
                // Right half (My Postings)
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

@Composable
fun FindJobsTab(
    state: CareerUiState,
    viewModel: CareerViewModel,
    navController: NavController?
) {
    when {
        state.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hata: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.onSurface
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
                contentPadding = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Job interests for you Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Job interests for you!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                        )
                        Text(
                            text = "Take a look at these job opportunities that we chose for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )

                        if (state.recommendedJobs.isEmpty()) {
                            Text(
                                text = "Henüz önerilen iş yok",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                state.recommendedJobs.forEach { job ->
                                    val user = state.users[job.userId]
                                    val hasApplied = job.id in state.appliedJobIds
                                    CareerJobCard(
                                        job = job,
                                        user = user,
                                        isSaved = false,
                                        hasApplied = hasApplied,
                                        onSaveClick = { viewModel.saveJob(job.id) },
                                        onRemoveClick = { viewModel.removeRecommendedJob(job.id) },
                                        onApplyClick = {
                                            viewModel.applyToJob(job.id, job.userId)
                                        },
                                        onClick = {
                                            if (navController != null && job.userId.isNotEmpty() && job.id.isNotEmpty()) {
                                                navController.navigate(Routes.userProfileWithJob(job.userId, job.id))
                                            }
                                        }
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )

                        if (state.savedJobs.isEmpty()) {
                            Text(
                                text = "Henüz kaydedilmiş iş yok",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                state.savedJobs.forEach { job ->
                                    val user = state.users[job.userId]
                                    val hasApplied = job.id in state.appliedJobIds
                                    CareerJobCard(
                                        job = job,
                                        user = user,
                                        isSaved = true,
                                        hasApplied = hasApplied,
                                        onSaveClick = { viewModel.unsaveJob(job.id) },
                                        onRemoveClick = { viewModel.unsaveJob(job.id) },
                                        onApplyClick = {
                                            viewModel.applyToJob(job.id, job.userId)
                                        },
                                        onClick = {
                                            if (navController != null && job.userId.isNotEmpty() && job.id.isNotEmpty()) {
                                                navController.navigate(Routes.userProfileWithJob(job.userId, job.id))
                                            }
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

@Composable
fun MyPostingsTab(
    state: CareerUiState,
    viewModel: CareerViewModel,
    navController: NavController?
) {
    when {
        state.selectedJobId != null -> {
            // Show applications for selected job
            val applications = state.applications[state.selectedJobId] ?: emptyList()
            ApplicationsScreen(
                jobId = state.selectedJobId!!,
                applications = applications,
                applicationUsers = state.applicationUsers,
                onBack = { viewModel.selectJob(null) },
                onUpdateStatus = { applicationId, status ->
                    viewModel.updateApplicationStatus(applicationId, status)
                }
            )
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.refresh() }) {
                    Text("Yeniden Dene", color = BrandYellow)
                }
            }
        }
        state.myJobs.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz iş paylaşmadınız",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.myJobs) { job ->
                    MyJobCard(
                        job = job,
                        applicationCount = state.applications[job.id]?.size ?: 0,
                        onClick = {
                            viewModel.selectJob(job.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CareerJobCard(
    job: Job,
    user: User? = null,
    isSaved: Boolean,
    hasApplied: Boolean,
    onSaveClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onApplyClick: () -> Unit,
    onClick: () -> Unit = {},
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isHighlighted) {
                    Modifier.border(width = 2.dp, color = BrandYellow, shape = RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        if (job.company.isNotBlank()) {
                            Text(
                                text = job.company.take(2).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = BrandYellow,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
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
                    Text(
                        text = if (job.company.isNotBlank()) "${job.title} – ${job.company}" else job.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (job.location.isNotBlank()) {
                            Text(
                                text = job.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        if (job.workType.isNotBlank()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = job.workType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Actions (Right)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Kaydı kaldır" else "Kaydet",
                            tint = if (isSaved) BrandYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kaldır",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Apply Button
            Spacer(modifier = Modifier.height(12.dp))
            if (hasApplied) {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Applied",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Apply",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MyJobCard(
    job: Job,
    applicationCount: Int,
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
                    if (job.company.isNotBlank()) {
                        Text(
                            text = job.company.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandYellow,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (job.company.isNotBlank()) "${job.title} – ${job.company}" else job.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$applicationCount applicant${if (applicationCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ApplicationsScreen(
    jobId: String,
    applications: List<JobApplication>,
    applicationUsers: Map<String, User>,
    onBack: () -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Applicants",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        // Applications List
        if (applications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz başvuru yok",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(applications.size) { index ->
                    val application = applications[index]
                    ApplicationCard(
                        application = application,
                        user = applicationUsers[application.applicantId],
                        onApprove = {
                            onUpdateStatus(application.id, "accepted")
                        },
                        onReject = {
                            onUpdateStatus(application.id, "rejected")
                        }
                    )
                    if (index < applications.size - 1) {
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    application: JobApplication,
    user: User?,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image
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
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user?.name?.take(1)?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandYellow,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = listOfNotNull(user?.name, user?.surname).joinToString(" ").takeIf { it.isNotBlank() } ?: "Unknown User",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            if (user?.department != null && user.department.isNotBlank()) {
                Text(
                    text = user.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Actions: X (Reject) and Approve button
        if (application.status == "pending") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // X (Reject) Icon
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reddet",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Approve Button (sarı çerçeve)
                OutlinedButton(
                    onClick = onApprove,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandYellow),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandYellow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Kabul Et",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Status badge for accepted/rejected
            val statusColor = when (application.status) {
                "accepted" -> Color(0xFF4CAF50)
                "rejected" -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
            Text(
                text = when (application.status) {
                    "accepted" -> "Kabul Edildi"
                    "rejected" -> "Reddedildi"
                    else -> application.status.replaceFirstChar { it.uppercase() }
                },
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
