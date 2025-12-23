package com.example.proto7hive.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.example.proto7hive.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proto7hive.ui.home.HomeFeedRoute
import com.example.proto7hive.ui.connections.ConnectionsRoute
import com.example.proto7hive.ui.createpost.CreatePostRoute
import com.example.proto7hive.ui.jobs.JobsRoute
import com.example.proto7hive.ui.profile.ProfileRoute
import com.example.proto7hive.ui.auth.WelcomeScreen
import com.example.proto7hive.ui.auth.LoginScreen
import com.example.proto7hive.ui.auth.SignUpFlowScreen
import com.example.proto7hive.ui.auth.ForgotPasswordScreen

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
    
    // Detay sayfaları (ileride gerekebilir)
    const val PROJECT_DETAIL = "project/{projectId}"
    const val POST_DETAIL = "post/{postId}"
    const val JOB_DETAIL = "job/{jobId}"

    fun projectDetail(projectId: String) = "project/$projectId"
    fun postDetail(postId: String) = "post/$postId"
    fun jobDetail(jobId: String) = "job/$jobId"
}

@Composable
fun RootScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var homeRefreshKey by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = { 
            if (currentRoute !in listOf(Routes.WELCOME, Routes.LOGIN, Routes.SIGN_UP, Routes.FORGOT_PASSWORD)) {
                BottomBar(navController) 
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
                HomeFeedRoute(key = homeRefreshKey)
            }
            composable(Routes.CONNECTIONS) { 
                ConnectionsRoute()
            }
            composable(Routes.CREATE_POST) { 
                CreatePostRoute(
                    navController = navController,
                    onPostCreated = {
                        // Post oluşturulunca Home'e git ve feed'i yenile
                        homeRefreshKey++ // Refresh key'i değiştir
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.JOBS) { 
                JobsRoute()
            }
            composable(Routes.PROFILE) { 
                ProfileRoute(
                    onNavigateToOnboarding = { 
                        navController.navigate(Routes.WELCOME) { 
                            popUpTo(0) { inclusive = true } 
                        } 
                    }
                ) 
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val items = listOf(
        Routes.HOME,
        Routes.CONNECTIONS,
        Routes.CREATE_POST,
        Routes.JOBS,
        Routes.PROFILE,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = com.example.proto7hive.ui.theme.BrandBackgroundDark
    ) {
        items.forEach { route ->
            val (iconRes, iconVector, label) = when (route) {
                Routes.HOME -> Triple(R.drawable.ic_homefeed, null, "Home")
                Routes.CONNECTIONS -> Triple(R.drawable.ic_connections_icon, null, "Connections")
                Routes.CREATE_POST -> Triple(R.drawable.ic_add_hexagon, null, "Share")
                Routes.JOBS -> Triple(R.drawable.ic_jobs_icon, null, "Jobs")
                Routes.PROFILE -> Triple(null, Icons.Default.AccountCircle, "Profile")
                else -> Triple(null, Icons.Default.Home, route)
            }
            val isSelected = currentRoute == route
            val iconColor = if (isSelected) com.example.proto7hive.ui.theme.BrandYellow else Color.White
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { if (!isSelected) navController.navigate(route) },
                label = { Text(label) },
                icon = { 
                    if (iconRes != null) {
                        // Drawable icon kullan (Home ve Create Post)
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(iconColor),
                            contentScale = ContentScale.Fit
                        )
                    } else if (iconVector != null) {
                        // Material Icon kullan
                        Icon(
                            imageVector = iconVector, 
                            contentDescription = label,
                            tint = iconColor
                        )
                    }
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = com.example.proto7hive.ui.theme.BrandYellow,
                    selectedTextColor = com.example.proto7hive.ui.theme.BrandYellow,
                    indicatorColor = com.example.proto7hive.ui.theme.BrandYellow.copy(alpha = 0.2f),
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White
                )
            )
        }
    }
}

