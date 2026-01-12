package com.example.proto7hive.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proto7hive.R

@Composable
fun AboutRoute(
    onBack: () -> Unit
) {
    AboutScreen(onBack = onBack)
}

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            // Header with Back Arrow and Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_logo_7hive),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .height(32.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Title "About Us"
            Text(
                text = "About Us",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Description
            AboutSection(
                title = "7Hive",
                version = "Version 0.1",
                content = "7Hive is a professional networking platform designed to connect students, professionals, and businesses. Share your work, discover career opportunities, and build meaningful connections in the hive."
            )

            // Terms of Use
            AboutSection(
                title = "Terms of Use",
                content = "By using 7Hive, you agree to our terms and conditions. Users must be respectful and professional in all interactions. Any misuse of the platform may result in account suspension or termination. We reserve the right to modify these terms at any time."
            )

            // Privacy Policy
            AboutSection(
                title = "Privacy Policy",
                content = "We are committed to protecting your privacy. Your personal information is securely stored and will never be shared with third parties without your consent. We use industry-standard encryption to protect your data. You have the right to access, modify, or delete your personal information at any time."
            )

            // Licenses
            AboutSection(
                title = "Open Source Licenses",
                content = "7Hive uses various open-source libraries and frameworks. This application is built with Kotlin, Jetpack Compose, Firebase, and other open-source technologies. We are grateful to the open-source community for their contributions."
            )

            // Contact
            AboutSection(
                title = "Contact Us",
                content = "For support, feedback, or inquiries, please contact us at:\nsupport@7hive.com\n\nWe typically respond within 24-48 hours during business days."
            )

            // Copyright
            Text(
                text = "© 2026 7Hive. All rights reserved.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun AboutSection(
    title: String,
    version: String? = null,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (version != null) {
                Text(
                    text = version,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = content,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
