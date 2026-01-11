package com.example.proto7hive.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proto7hive.R
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proto7hive.ui.home.HomeFeedRoute
import com.example.proto7hive.ui.connections.ConnectionsRoute
import com.example.proto7hive.ui.createpost.CreatePostRoute
import com.example.proto7hive.ui.jobs.JobsRoute
import com.example.proto7hive.ui.profile.ProfileRoute
import com.example.proto7hive.ui.profile.UserProfileRoute
import com.example.proto7hive.ui.search.SearchScreen
import com.example.proto7hive.ui.auth.WelcomeScreen
import com.example.proto7hive.ui.auth.LoginScreen
import com.example.proto7hive.ui.auth.SignUpFlowScreen
import com.example.proto7hive.ui.auth.ForgotPasswordScreen
import com.example.proto7hive.ui.comments.CommentsRoute
import com.example.proto7hive.ui.notifications.NotificationRoute
import com.example.proto7hive.ui.settings.SettingsRoute
import com.example.proto7hive.ui.auth.AuthViewModel
import com.example.proto7hive.ui.auth.AuthViewModelFactory
import com.example.proto7hive.data.AuthRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Person

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home" // Portfolio yerine
    const val CONNECTIONS = "connections" // Projects yerine
    const val CREATE_POST = "create_post" // Announcements yerine
    const val JOBS = "jobs" // Matches yerine
    const val PROFILE = "profile"
    const val USER_PROFILE = "user/{userId}"
    const val USER_PROFILE_WITH_JOB = "user/{userId}/job/{jobId}"
    
    // Detay sayfaları (ileride gerekebilir)
    const val PROJECT_DETAIL = "project/{projectId}"
    const val POST_DETAIL = "post/{postId}"
    const val JOB_DETAIL = "job/{jobId}"
    const val COMMENTS_POST = "comments/post/{postId}"
    const val COMMENTS_JOB = "comments/job/{jobId}"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    fun projectDetail(projectId: String) = "project/$projectId"
    fun postDetail(postId: String) = "post/$postId"
    fun jobDetail(jobId: String) = "job/$jobId"
    fun commentsPost(postId: String) = "comments/post/$postId"
    fun commentsJob(jobId: String) = "comments/job/$jobId"
    fun userProfile(userId: String) = "user/$userId"
    fun userProfileWithJob(userId: String, jobId: String) = "user/$userId/job/$jobId"
}

@Composable
fun RootScaffold(
    onThemeChange: (Boolean) -> Unit = {},
    isDarkTheme: Boolean = true
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository()))
    
    // Profile refresh key - Settings'ten dönüldüğünde artırılır
    var profileRefreshKey by remember { mutableStateOf(0) }
    val authState by authViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { 
            if (currentRoute !in listOf(
                Routes.WELCOME, 
                Routes.LOGIN, 
                Routes.SIGN_UP, 
                Routes.FORGOT_PASSWORD,
                Routes.COMMENTS_POST,
                Routes.COMMENTS_JOB,
                Routes.SETTINGS,
                Routes.SEARCH,
                Routes.NOTIFICATIONS
            )) {
                BottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    currentUserId = authState.user?.uid,
                    isDarkTheme = isDarkTheme
                ) 
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WELCOME,
            modifier = Modifier
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLoginClick = {
                        navController.navigate(Routes.LOGIN)
                    },
                    onSignUpClick = {
                        navController.navigate(Routes.SIGN_UP)
                    }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.SIGN_UP) {
                SignUpFlowScreen(
                    onSignUpComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.HOME) { 
                HomeFeedRoute(
                    navController = navController
                )
            }
            composable(Routes.CONNECTIONS) { 
                ConnectionsRoute(navController = navController)
            }
            composable(Routes.CREATE_POST) { 
                CreatePostRoute(
                    navController = navController,
                    onPostCreated = {
                        // Post oluşturulunca Home'e git
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.JOBS) { 
                JobsRoute(navController = navController)
            }
            composable(Routes.PROFILE) { 
                ProfileRoute(
                    onNavigateToOnboarding = { 
                        navController.navigate(Routes.WELCOME) { 
                            popUpTo(0) { inclusive = true } 
                        } 
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Routes.SEARCH)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Routes.NOTIFICATIONS)
                    },
                    refreshKey = profileRefreshKey
                ) 
            }
            composable(Routes.SEARCH) {
                SearchScreen(navController = navController)
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationRoute(navController = navController)
            }
            composable(Routes.SETTINGS) {
                SettingsRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAccountInformation = {
                        // TODO: Account Information ekranı eklenecek
                    },
                    onNavigateToSecurity = {
                        // TODO: Security ekranı eklenecek
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Routes.NOTIFICATIONS)
                    },
                    onThemeChange = onThemeChange,
                    isDarkTheme = isDarkTheme,
                    onNavigateToLanguage = {
                        // TODO: Language ekranı eklenecek
                    },
                    onNavigateToAbout = {
                        // TODO: About ekranı eklenecek
                    },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Routes.COMMENTS_POST,
                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")
                CommentsRoute(
                    postId = postId,
                    jobId = null,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Routes.COMMENTS_JOB,
                arguments = listOf(
                    navArgument("jobId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId")
                CommentsRoute(
                    postId = null,
                    jobId = jobId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Routes.USER_PROFILE,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId != null) {
                    UserProfileRoute(
                        userId = userId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSearch = {
                            navController.navigate(Routes.SEARCH)
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Routes.NOTIFICATIONS)
                        }
                    )
                }
            }
            composable(
                route = Routes.USER_PROFILE_WITH_JOB,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("jobId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                val jobId = backStackEntry.arguments?.getString("jobId")
                if (userId != null && jobId != null) {
                    UserProfileRoute(
                        userId = userId,
                        selectedJobId = jobId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSearch = {
                            navController.navigate(Routes.SEARCH)
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Routes.NOTIFICATIONS)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    navController: NavHostController,
    currentRoute: String?,
    currentUserId: String?,
    isDarkTheme: Boolean = true
) {
    val items = listOf(
        Routes.HOME,
        Routes.CONNECTIONS,
        Routes.CREATE_POST,
        Routes.JOBS,
        Routes.PROFILE,
    )
    
    // Kullanıcı profil bilgisini al - currentUserId değiştiğinde state'i sıfırla
    var currentUser by remember { mutableStateOf<com.example.proto7hive.model.User?>(null) }
    LaunchedEffect(currentUserId) {
        // Önce null yap ki eski kullanıcı resmi gözükmesin
        currentUser = null
        if (currentUserId != null) {
            val userRepository = com.example.proto7hive.data.FirestoreUserRepository()
            currentUser = userRepository.getUser(currentUserId)
        }
    }
    
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { route ->
                val (iconRes, iconVector, contentDesc) = when (route) {
                    Routes.HOME -> Triple(R.drawable.ic_homefeed, null, "Home")
                    Routes.CONNECTIONS -> Triple(R.drawable.ic_connections_icon, null, "Connections")
                    Routes.CREATE_POST -> Triple(R.drawable.ic_add_hexagon, null, "Share")
                    Routes.JOBS -> Triple(R.drawable.ic_jobs_icon, null, "Jobs")
                    Routes.PROFILE -> Triple(null, Icons.Default.AccountCircle, "Profile")
                    else -> Triple(null, Icons.Default.Home, route)
                }
                val isSelected = when (route) {
                    Routes.PROFILE -> currentRoute == route || 
                        (currentRoute != null && (currentRoute.startsWith("user/") || currentRoute == Routes.PROFILE))
                    else -> currentRoute == route
                }
                val iconColor = if (isSelected) {
                    com.example.proto7hive.ui.theme.BrandYellow
                } else {
                    if (isDarkTheme) Color.White else Color(0xFF363636)
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { if (!isSelected) navController.navigate(route) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    if (iconRes != null) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = contentDesc,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(iconColor),
                            contentScale = ContentScale.Fit
                        )
                    } else if (route == Routes.PROFILE) {
                        // Profile icon - profil resmi varsa göster, yoksa default icon (ProfileScreen ile aynı yapı)
                        val borderColor = if (isSelected) {
                            com.example.proto7hive.ui.theme.BrandYellow
                        } else {
                            if (isDarkTheme) Color.Black else Color(0xFF363636)
                        }
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer border circle (24dp)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 2.dp,
                                        color = borderColor,
                                        shape = CircleShape
                                    )
                            )
                            
                            // Inner profile image (20dp to account for 2dp border on each side)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentUser?.profileImageUrl != null && currentUser?.profileImageUrl?.isNotBlank() == true) {
                                    AsyncImage(
                                        model = currentUser!!.profileImageUrl, // ProfileScreen'deki gibi direkt URL
                                        contentDescription = contentDesc,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = contentDesc,
                                        tint = iconColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    } else if (iconVector != null) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = contentDesc,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Alt çizgi (sadece seçili olan için)
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(2.dp)
                            .background(
                                if (isSelected) com.example.proto7hive.ui.theme.BrandYellow 
                                else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}

