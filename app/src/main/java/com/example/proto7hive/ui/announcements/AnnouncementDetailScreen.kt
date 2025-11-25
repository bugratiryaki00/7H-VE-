package com.example.proto7hive.ui.announcements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proto7hive.data.AssetsAnnouncementRepository

@Composable
fun AnnouncementDetailRoute(announcementId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = AssetsAnnouncementRepository(context.assets)
    val vm = androidx.lifecycle.viewmodel.compose.viewModel<AnnouncementsViewModel>(factory = AnnouncementsViewModelFactory(repo))
    val state by vm.uiState.collectAsState()
    val ann = state.items.firstOrNull { it.id == announcementId }
    if (ann == null) {
        Text(text = "Duyuru bulunamadı")
        return
    }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (ann.imageUrl != null) {
            AsyncImage(
                model = ann.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        Text(text = ann.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        Text(text = ann.body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        Text(text = "Tarih: ${ann.dateIso}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
    }
}


