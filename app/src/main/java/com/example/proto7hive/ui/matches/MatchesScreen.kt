package com.example.proto7hive.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proto7hive.data.AssetsMatchingRepository
import com.example.proto7hive.data.FirestoreMatchingRepository
import com.example.proto7hive.data.AssetsUserRepository
import com.example.proto7hive.model.MatchSuggestion
import androidx.compose.runtime.remember

@Composable
fun MatchesRoute(viewModel: MatchesViewModel? = null, forUserId: String = "u1") {
    val vm = viewModel ?: run {
        val context = androidx.compose.ui.platform.LocalContext.current
        val factory = MatchesViewModelFactory(FirestoreMatchingRepository(), forUserId)
        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    }
    val state by vm.uiState.collectAsState()
    when {
        state.isLoading -> Loading()
        state.errorMessage != null -> ErrorText(state.errorMessage ?: "Hata")
        state.items.isEmpty() -> EmptyState()
        else -> MatchesList(state.items)
    }
}

@Composable
private fun MatchesList(items: List<MatchSuggestion>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userRepo = remember { AssetsUserRepository(context.assets) }
    // basit senkron kullanım için produceState yerine LaunchedEffect ile bir kez yükleyelim
    val (users, setUsers) = remember { androidx.compose.runtime.mutableStateOf(emptyMap<String, String>()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val list = userRepo.getUsers()
        setUsers(list.associate { it.id to it.name })
    }
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(elevation = CardDefaults.cardElevation(3.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val name = users[item.suggestedUserId] ?: item.suggestedUserId
                    Text(text = "Öneri: $name", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Skor: ${"%.2f".format(item.score)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable private fun Loading() { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Yükleniyor…", modifier = Modifier.padding(top = 12.dp)) } }
@Composable private fun ErrorText(msg: String) { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Hata: $msg", style = MaterialTheme.typography.bodyMedium) } }
@Composable private fun EmptyState() { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Henüz öneri yok." ); Text("Profilinizi güncellemeyi deneyin.", style = MaterialTheme.typography.bodySmall) } }


