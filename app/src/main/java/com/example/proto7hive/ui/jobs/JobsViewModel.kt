package com.example.proto7hive.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.JobRepository
import com.example.proto7hive.data.FirestoreJobRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class JobsUiState(
    val isLoading: Boolean = true,
    val recommendedJobs: List<Job> = emptyList(),
    val savedJobs: List<Job> = emptyList(),
    val users: Map<String, User> = emptyMap(), // userId -> User mapping
    val errorMessage: String? = null
)

class JobsViewModel(
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState: StateFlow<JobsUiState> = _uiState

    init {
        loadJobs()
    }

    fun loadJobs() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = JobsUiState(
                        isLoading = false,
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }

                // Tüm işleri al (jobs koleksiyonundan)
                // Artık work paylaşırken direkt Job oluşturulduğu için sadece jobs koleksiyonundan alıyoruz
                val allJobsCombined = jobRepository.getJobs()
                
                // Tüm job'ların userId'lerini topla
                val userIds = allJobsCombined.map { it.userId }.distinct().filter { it.isNotBlank() }
                
                // Kullanıcı bilgilerini yükle
                val usersMap = userIds.associateWith { userId ->
                    try {
                        userRepository.getUser(userId) ?: User(id = userId, name = "Bilinmeyen Kullanıcı", email = "")
                    } catch (e: Exception) {
                        User(id = userId, name = "Bilinmeyen Kullanıcı", email = "")
                    }
                }
                
                // Kaydedilen iş ID'lerini al
                val savedJobIds = jobRepository.getSavedJobs(currentUser.uid)
                
                // Kaydedilen işleri filtrele
                val savedJobsList = allJobsCombined.filter { 
                    it.id in savedJobIds || it.id.replace("post_", "") in savedJobIds 
                }
                
                // Önerilen işler = kaydedilenler hariç tüm işler
                val recommendedJobsList = allJobsCombined.filter { 
                    it.id !in savedJobIds && it.id.replace("post_", "") !in savedJobIds
                }

                _uiState.value = JobsUiState(
                    isLoading = false,
                    recommendedJobs = recommendedJobsList,
                    savedJobs = savedJobsList,
                    users = usersMap
                )
            } catch (t: Throwable) {
                _uiState.value = JobsUiState(
                    isLoading = false,
                    errorMessage = t.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun saveJob(jobId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                jobRepository.saveJob(currentUser.uid, jobId)
                loadJobs() // Refresh list
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "İş kaydedilirken hata oluştu"
                )
            }
        }
    }

    fun unsaveJob(jobId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                jobRepository.unsaveJob(currentUser.uid, jobId)
                loadJobs() // Refresh list
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "İş kaydı kaldırılırken hata oluştu"
                )
            }
        }
    }

    fun removeRecommendedJob(jobId: String) {
        // Önerilen işi listeden kaldır (sadece UI'dan)
        _uiState.value = _uiState.value.copy(
            recommendedJobs = _uiState.value.recommendedJobs.filter { it.id != jobId }
        )
    }

    fun refresh() {
        loadJobs()
    }
}

class JobsViewModelFactory(
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobsViewModel::class.java)) {
            return JobsViewModel(jobRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

