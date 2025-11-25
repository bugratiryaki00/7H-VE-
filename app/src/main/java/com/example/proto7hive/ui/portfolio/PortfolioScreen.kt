package com.example.proto7hive.ui.portfolio

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
import com.example.proto7hive.data.AssetsPortfolioRepository
import com.example.proto7hive.data.FirestorePortfolioRepository
import com.example.proto7hive.model.PortfolioCard

@Composable
fun PortfolioRoute(viewModel: PortfolioViewModel? = null): Unit {
    val vm = viewModel ?: run {
        // Varsayılan olarak CompositionLocal yoksa mock'a düşmemesi için composable üzerinde context ile factory kullanalım
        val context = androidx.compose.ui.platform.LocalContext.current
        val factory = PortfolioViewModelFactory(FirestorePortfolioRepository())
        androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    }
    val state by vm.uiState.collectAsState()
    when {
        state.isLoading -> Loading()
        state.errorMessage != null -> ErrorText(state.errorMessage ?: "Hata")
        state.items.isEmpty() -> EmptyState()
        else -> PortfolioList(state.items)
    }
}

@Composable
private fun PortfolioList(items: List<PortfolioCard>) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            PortfolioCardItem(item)
        }
    }
}

@Composable
private fun PortfolioCardItem(item: PortfolioCard) {
    Card(
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.bio ?: "(Bio yok)",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.projects.isNotEmpty()) {
                Text(
                    text = "Projeler: " + item.projects.joinToString { it.title },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun Loading() {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Text(text = "Yükleniyor…", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ErrorText(msg: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Hata: $msg", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyState() {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Henüz portföy bulunmuyor.")
        Text(text = "Bir portföy ekleyerek başlayın.", style = MaterialTheme.typography.bodySmall)
    }
}


