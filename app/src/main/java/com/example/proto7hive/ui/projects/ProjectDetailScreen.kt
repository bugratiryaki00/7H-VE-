package com.example.proto7hive.ui.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proto7hive.data.AssetsProjectRepository
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@Composable
fun ProjectDetailRoute(projectId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = AssetsProjectRepository(context.assets)
    val vm = androidx.lifecycle.viewmodel.compose.viewModel<ProjectsViewModel>(factory = ProjectsViewModelFactory(repo))
    val state by vm.uiState.collectAsState()
    val project = state.items.firstOrNull { it.id == projectId }
    if (project == null) {
        Text(text = "Proje bulunamadı")
        return
    }
    val (roles, setRoles) = remember { mutableStateOf(emptyList<com.example.proto7hive.model.Role>()) }
    LaunchedEffect(projectId) {
        setRoles(repo.getRoles(projectId))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (project.imageUrl != null) {
            AsyncImage(
                model = project.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        Text(text = project.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        Text(text = project.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        if (project.tags.isNotEmpty()) {
            Text(text = "Etiketler: ${project.tags.joinToString()}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
        if (roles.isNotEmpty()) {
            Text(text = "Açık Roller", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            roles.forEach { role ->
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = role.title, style = MaterialTheme.typography.bodyLarge)
                    role.requiredSkills.take(3).forEach { skill ->
                        AssistChip(
                            onClick = {},
                            label = { Text(skill) },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                }
                Button(onClick = {
                    scope.launch { snackbarHostState.showSnackbar("Başvuru gönderildi: ${role.title}") }
                }, modifier = Modifier.padding(start = 0.dp, top = 8.dp)) {
                    Text("Başvuru Gönder")
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}


