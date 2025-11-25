package com.example.proto7hive.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proto7hive.ui.portfolio.PortfolioRoute
import com.example.proto7hive.ui.projects.ProjectsRoute
import com.example.proto7hive.ui.announcements.AnnouncementsRoute
import com.example.proto7hive.ui.matches.MatchesRoute
import com.example.proto7hive.ui.profile.ProfileRoute
import com.example.proto7hive.ui.onboarding.OnboardingRoute

object Routes {
    const val ONBOARDING = "onboarding"
    const val PORTFOLIO = "portfolio"
    const val PROJECTS = "projects"
    const val PROJECT_DETAIL = "project/{projectId}"
    const val ANNOUNCEMENTS = "announcements"
    const val ANNOUNCEMENT_DETAIL = "announcement/{announcementId}"
    const val MATCHES = "matches"
    const val PROFILE = "profile"

    fun projectDetail(projectId: String) = "project/$projectId"
    fun announcementDetail(id: String) = "announcement/$id"
}

@Composable
fun RootScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = { if (currentRoute != Routes.ONBOARDING) BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ONBOARDING,
            modifier = Modifier
        ) {
            composable(Routes.ONBOARDING) { OnboardingRoute(onContinue = { navController.navigate(Routes.PORTFOLIO) { popUpTo(Routes.ONBOARDING) { inclusive = true } } }) }
            composable(Routes.PORTFOLIO) { PortfolioRoute() }
            composable(Routes.PROJECTS) { ProjectsRoute(onProjectClick = { id -> navController.navigate(Routes.projectDetail(id)) }) }
            composable(Routes.PROJECT_DETAIL) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("projectId") ?: ""
                com.example.proto7hive.ui.projects.ProjectDetailRoute(projectId = id)
            }
            composable(Routes.ANNOUNCEMENTS) { AnnouncementsRoute(onAnnouncementClick = { id -> navController.navigate(Routes.announcementDetail(id)) }) }
            composable(Routes.ANNOUNCEMENT_DETAIL) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("announcementId") ?: ""
                com.example.proto7hive.ui.announcements.AnnouncementDetailRoute(announcementId = id)
            }
            composable(Routes.MATCHES) { MatchesRoute() }
            composable(Routes.PROFILE) { 
                ProfileRoute(
                    onNavigateToOnboarding = { 
                        navController.navigate(Routes.ONBOARDING) { 
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
        Routes.PORTFOLIO,
        Routes.PROJECTS,
        Routes.ANNOUNCEMENTS,
        Routes.MATCHES,
        Routes.PROFILE,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        items.forEach { route ->
            val (icon, label) = when (route) {
                Routes.PORTFOLIO -> Icons.Default.Home to "Portfolio"
                Routes.PROJECTS -> Icons.Default.Work to "Projects"
                Routes.ANNOUNCEMENTS -> Icons.Default.Article to "Announcements"
                Routes.MATCHES -> Icons.Default.Diversity3 to "Matches"
                Routes.PROFILE -> Icons.Default.AccountCircle to "Profile"
                else -> Icons.Default.Home to route
            }
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { if (currentRoute != route) navController.navigate(route) },
                label = { Text(label) },
                icon = { Icon(imageVector = icon, contentDescription = label) }
            )
        }
    }
}

@Composable
private fun SimpleScreen(title: String) {
    Text(text = title)
}


