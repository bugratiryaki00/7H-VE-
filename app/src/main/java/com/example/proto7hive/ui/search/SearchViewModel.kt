package com.example.proto7hive.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.FirestorePostRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.data.FirestoreJobRepository
import com.example.proto7hive.data.PostRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.JobRepository
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.User
import com.example.proto7hive.model.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class SearchUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val posts: List<Post> = emptyList(),
    val jobs: List<Job> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private var searchJob: kotlinx.coroutines.Job? = null

    fun search(query: String) {
        // Önceki arama işlemini iptal et
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState()
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState(isLoading = true)

            try {
                // Debounce için kısa bir gecikme (kullanıcı yazmayı bitirdiğinde arama yap)
                delay(300)

                // Paralel olarak tüm aramaları yap
                val usersResult = userRepository.searchUsers(query)
                val postsResult = postRepository.searchPosts(query)
                val jobsResult = jobRepository.searchJobs(query)

                _uiState.value = SearchUiState(
                    isLoading = false,
                    users = usersResult,
                    posts = postsResult,
                    jobs = jobsResult
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Arama yapılırken hata oluştu"
                )
            }
        }
    }
}

class SearchViewModelFactory(
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val postRepository: PostRepository = FirestorePostRepository(),
    private val jobRepository: JobRepository = FirestoreJobRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(userRepository, postRepository, jobRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

