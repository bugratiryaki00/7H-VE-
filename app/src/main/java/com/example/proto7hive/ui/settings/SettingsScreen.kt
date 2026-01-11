package com.example.proto7hive.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proto7hive.R
import com.example.proto7hive.ui.components.SearchBar
import com.example.proto7hive.ui.theme.BrandYellow

data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isExit: Boolean = false,
    val isToggle: Boolean = false,
    val toggleValue: Boolean = false,
    val onToggleChange: ((Boolean) -> Unit)? = null
)

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onNavigateToAccountInformation: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onThemeChange: (Boolean) -> Unit = {},
    isDarkTheme: Boolean = true,
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    SettingsScreen(
        onBack = onBack,
        onNavigateToAccountInformation = onNavigateToAccountInformation,
        onNavigateToSecurity = onNavigateToSecurity,
        onNavigateToNotifications = onNavigateToNotifications,
        onThemeChange = onThemeChange,
        isDarkTheme = isDarkTheme,
        onNavigateToLanguage = onNavigateToLanguage,
        onNavigateToAbout = onNavigateToAbout,
        onLogout = onLogout
    )
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAccountInformation: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onThemeChange: (Boolean) -> Unit = {},
    isDarkTheme: Boolean = true,
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Back Arrow, Logo and "hive" text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Back Arrow - Sol tarafta
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Logo ve "hive" text - Ortada
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_7hive),
                    contentDescription = "Logo",
                    modifier = Modifier.height(32.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "hive",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Title "Settings"
        Text(
            text = "Settings",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // SearchBar
        SearchBar(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = "Search",
            onSearchClick = {},
            showNotificationIcon = false
        )

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ACCOUNT Section
            item {
                SettingsSection(
                    title = "ACCOUNT",
                    items = listOf(
                        SettingsItem(
                            title = "Account Information",
                            subtitle = "E-mail, password, two-step confirmation",
                            icon = Icons.Outlined.AdminPanelSettings, // Person + Lock icon
                            onClick = onNavigateToAccountInformation
                        ),
                        SettingsItem(
                            title = "Security",
                            subtitle = "Profile visibility and blocked people",
                            icon = Icons.Outlined.Lock,
                            onClick = onNavigateToSecurity
                        )
                    )
                )
            }

            // PREFERENCES Section
            item {
                SettingsSection(
                    title = "PREFERENCES",
                    items = listOf(
                        SettingsItem(
                            title = "Notifications",
                            subtitle = "Bees, career, interactions",
                            icon = Icons.Outlined.Notifications,
                            onClick = onNavigateToNotifications
                        ),
                        SettingsItem(
                            title = "Theme",
                            subtitle = "Dark theme, light theme",
                            icon = Icons.Outlined.Brightness4,
                            onClick = { },
                            isToggle = true,
                            toggleValue = isDarkTheme,
                            onToggleChange = onThemeChange
                        ),
                        SettingsItem(
                            title = "Language",
                            subtitle = "English",
                            icon = Icons.Outlined.Language,
                            onClick = onNavigateToLanguage
                        )
                    )
                )
            }

            // SUPPORT Section
            item {
                SettingsSection(
                    title = "SUPPORT",
                    items = listOf(
                        SettingsItem(
                            title = "About",
                            subtitle = "Version, terms of use, licences",
                            icon = Icons.Outlined.Info,
                            onClick = onNavigateToAbout
                        ),
                        SettingsItem(
                            title = "Exit",
                            subtitle = "",
                            icon = Icons.Outlined.ExitToApp,
                            onClick = onLogout,
                            isExit = true
                        )
                    )
                )
            }
        }

        // Footer: "Hive v0.1 - © 2026" - Sol alt köşe
        Text(
            text = "Hive v0.1 - © 2026",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Start
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Items
        items.forEach { item ->
            SettingsItemCard(item = item)
        }
    }
}

@Composable
fun SettingsItemCard(item: SettingsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !item.isToggle) { item.onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (item.isExit) {
            androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon - Sarı çember içinde beyaz ikon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.title == "Account Information") {
                    // Person icon (büyük, tema rengi)
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    // Lock icon (küçük, sağ alt köşede, tema rengi)
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                } else {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Toggle Button (Theme için)
            if (item.isToggle) {
                Switch(
                    checked = item.toggleValue,
                    onCheckedChange = { item.onToggleChange?.invoke(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

