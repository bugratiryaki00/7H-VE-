package com.example.proto7hive.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proto7hive.data.AssetsProjectRepository
import com.example.proto7hive.data.FirestoreProjectRepository
import com.example.proto7hive.model.Project

@Composable
fun ProjectsRoute(viewModel: ProjectsViewModel? = null, onProjectClick: (String) -> Unit = {}) {
    val vm = viewModel ?: run {
        val context = androidx.compose.ui.platform.LocalContext.current
        val factory = ProjectsViewModelFactory(FirestoreProjectRepository())
        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    }
    val state by vm.uiState.collectAsState()
    when {
        state.isLoading -> Loading()
        state.errorMessage != null -> ErrorText(state.errorMessage ?: "Hata")
        state.items.isEmpty() -> EmptyState()
        else -> ProjectsWithFilters(state.items, onProjectClick)
    }
}

@Composable
private fun ProjectsWithFilters(items: List<Project>, onProjectClick: (String) -> Unit) {
    val allTags = remember(items) { items.flatMap { it.tags }.distinct().sorted() }
    val (selectedTag, setSelectedTag) = remember { mutableStateOf<String?>(null) }
    val (query, setQuery) = remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = setQuery,
            label = { Text("Ara") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        Row(modifier = Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { setSelectedTag(null) },
                label = { Text("Tümü") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedTag == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            )
            allTags.forEach { tag ->
                AssistChip(
                    onClick = { setSelectedTag(if (selectedTag == tag) null else tag) },
                    label = { Text(tag) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selectedTag == tag) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        val filtered = remember(items, selectedTag, query) {
            items.filter { p ->
                (selectedTag == null || p.tags.contains(selectedTag)) &&
                (query.isBlank() || p.title.contains(query, ignoreCase = true) || p.description.contains(query, ignoreCase = true))
            }
        }

        ProjectsList(filtered, onProjectClick)
    }
}

@Composable
private fun ProjectsList(items: List<Project>, onProjectClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(elevation = CardDefaults.cardElevation(3.dp), onClick = { onProjectClick(item.id) }) {
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
                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable private fun Loading() { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Yükleniyor…", modifier = Modifier.padding(top = 12.dp)) } }
@Composable private fun ErrorText(msg: String) { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Hata: $msg", style = MaterialTheme.typography.bodyMedium) } }
@Composable private fun EmptyState() { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Henüz proje yok." ); Text("Yeni bir proje ekleyin.", style = MaterialTheme.typography.bodySmall) } }


