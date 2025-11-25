package com.example.proto7hive.ui.announcements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.data.AssetsAnnouncementRepository
import com.example.proto7hive.data.FirestoreAnnouncementRepository
import com.example.proto7hive.model.Announcement

@Composable
fun AnnouncementsRoute(viewModel: AnnouncementsViewModel? = null, onAnnouncementClick: (String) -> Unit = {}) {
    val vm = viewModel ?: run {
        val context = androidx.compose.ui.platform.LocalContext.current
        val factory = AnnouncementsViewModelFactory(FirestoreAnnouncementRepository())
        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    }
    val state by vm.uiState.collectAsState()
    when {
        state.isLoading -> Loading()
        state.errorMessage != null -> ErrorText(state.errorMessage ?: "Hata")
        state.items.isEmpty() -> EmptyState()
        else -> AnnouncementsList(state.items, onAnnouncementClick)
    }
}

@Composable
private fun AnnouncementsList(items: List<Announcement>, onAnnouncementClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(elevation = CardDefaults.cardElevation(3.dp), onClick = { onAnnouncementClick(item.id) }) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = item.body, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable private fun Loading() { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Yükleniyor…", modifier = Modifier.padding(top = 12.dp)) } }
@Composable private fun ErrorText(msg: String) { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Hata: $msg", style = MaterialTheme.typography.bodyMedium) } }
@Composable private fun EmptyState() { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Henüz duyuru yok." ); Text("Daha sonra tekrar kontrol edin.", style = MaterialTheme.typography.bodySmall) } }


