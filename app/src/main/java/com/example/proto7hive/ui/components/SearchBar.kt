package com.example.proto7hive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.proto7hive.ui.theme.BrandYellow

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearchClick: () -> Unit = {},
    showNotificationIcon: Boolean = true,
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = BrandYellow
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = BrandYellow,
                disabledBorderColor = BrandYellow,
                cursorColor = BrandYellow,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                disabledPlaceholderColor = Color.White.copy(alpha = 0.5f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            singleLine = true,
            readOnly = true,
            enabled = true
        )
        
        // Notification Icon
        if (showNotificationIcon) {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = BrandYellow
                )
            }
        }
    }
}

