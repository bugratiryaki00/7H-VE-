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
import androidx.compose.foundation.layout.size
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
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Bar (centered with max width) - Clickable Box wrapper
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.85f)
                .clickable { onSearchClick() }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BrandYellow,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = BrandYellow,
                    disabledBorderColor = BrandYellow,
                    cursorColor = BrandYellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    focusedPlaceholderColor = BrandYellow,
                    unfocusedPlaceholderColor = BrandYellow,
                    disabledPlaceholderColor = BrandYellow,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                singleLine = true,
                readOnly = true,
                enabled = false
            )
        }
        
        // Spacer before notification icon
        if (showNotificationIcon) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        
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

