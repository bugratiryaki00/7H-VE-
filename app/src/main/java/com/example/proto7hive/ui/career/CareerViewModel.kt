package com.example.proto7hive.ui.career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proto7hive.data.JobRepository
import com.example.proto7hive.data.FirestoreJobRepository
import com.example.proto7hive.data.JobApplicationRepository
import com.example.proto7hive.data.FirestoreJobApplicationRepository
import com.example.proto7hive.data.UserRepository
import com.example.proto7hive.data.FirestoreUserRepository
import com.example.proto7hive.data.NotificationRepository
import com.example.proto7hive.data.FirestoreNotificationRepository
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.JobApplication
import com.example.proto7hive.model.User
import com.example.proto7hive.model.Notification
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CareerUiState(
    val isLoading: Boolean = true,
    // Find Jobs Tab
    val recommendedJobs: List<Job> = emptyList(),
    val savedJobs: List<Job> = emptyList(),
    val users: Map<String, User> = emptyMap(),
    val appliedJobIds: Set<String> = emptySet(),
    // My Postings Tab
    val myJobs: List<Job> = emptyList(),
    val selectedJobId: String? = null,
    val applications: Map<String, List<JobApplication>> = emptyMap(),
    val applicationUsers: Map<String, User> = emptyMap(),
    val errorMessage: String? = null
)

class CareerViewModel(
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val jobApplicationRepository: JobApplicationRepository = FirestoreJobApplicationRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CareerUiState())
    val uiState: StateFlow<CareerUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = CareerUiState(
                        isLoading = false,
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }

                val allJobs = jobRepository.getJobs()

                val myJobsList = jobRepository.getJobsByUserId(currentUser.uid)
                val myJobIds = myJobsList.map { it.id }.toSet()

                val otherUsersJobs = allJobs.filter { it.id !in myJobIds }

                val userIds = allJobs.map { it.userId }.distinct().filter { it.isNotBlank() }

                val usersMap = userIds.associateWith { userId ->
                    try {
                        userRepository.getUser(userId) ?: User(id = userId, name = "Bilinmeyen Kullanıcı", email = "")
                    } catch (e: Exception) {
                        User(id = userId, name = "Bilinmeyen Kullanıcı", email = "")
                    }
                }

                // Kaydedilen iş ID'lerini al
                val savedJobIds = jobRepository.getSavedJobs(currentUser.uid)

                // Kaydedilen işleri filtrele (kendi işlerim hariç, sadece çalışan arayan işler - isJobPosting = true)
                val savedJobsList = otherUsersJobs.filter {
                    it.isJobPosting == true && (it.id in savedJobIds || it.id.replace("post_", "") in savedJobIds)
                }

                // Önerilen işler = kaydedilenler hariç diğer kullanıcıların işleri (kendi işlerim hariç, sadece çalışan arayan işler - isJobPosting = true)
                val recommendedJobsList = otherUsersJobs.filter {
                    it.isJobPosting == true && it.id !in savedJobIds && it.id.replace("post_", "") !in savedJobIds
                }

                // Başvurulan iş ID'lerini al
                val myApplications = jobApplicationRepository.getApplicationsByApplicantId(currentUser.uid)
                val appliedJobIdsSet = myApplications.map { it.jobId }.toSet()

                // Kendi işlerime gelen başvuruları say (başvuran sayısı için)
                val allMyApplications = jobApplicationRepository.getApplicationsByJobOwnerId(currentUser.uid)
                val applicationsMap = mutableMapOf<String, List<JobApplication>>()
                myJobsList.forEach { job ->
                    val jobApplications = allMyApplications.filter { it.jobId == job.id }
                    applicationsMap[job.id] = jobApplications
                }
                
                // Seçili işin başvuruları loadApplicationsForJob tarafından yüklenecek (eğer varsa)
                // Seçili işin başvurularını applicationsMap'e ekle (üzerine yaz)
                _uiState.value.selectedJobId?.let { selectedJobId ->
                    val selectedJobApplications = allMyApplications.filter { it.jobId == selectedJobId }
                    if (selectedJobApplications.isNotEmpty()) {
                        applicationsMap[selectedJobId] = selectedJobApplications
                    }
                }
                
                val applicationUsersMap = mutableMapOf<String, User>()

                _uiState.value = CareerUiState(
                    isLoading = false,
                    recommendedJobs = recommendedJobsList,
                    savedJobs = savedJobsList,
                    users = usersMap,
                    appliedJobIds = appliedJobIdsSet,
                    myJobs = myJobsList,
                    selectedJobId = _uiState.value.selectedJobId,
                    applications = applicationsMap,
                    applicationUsers = applicationUsersMap
                )
            } catch (t: Throwable) {
                _uiState.value = CareerUiState(
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
                loadData()
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
                loadData()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "İş kaydı kaldırılırken hata oluştu"
                )
            }
        }
    }

    fun applyToJob(jobId: String, jobOwnerId: String, message: String? = null) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                jobApplicationRepository.applyToJob(jobId, currentUser.uid, jobOwnerId, message)
                loadData()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Başvuru yapılırken hata oluştu"
                )
            }
        }
    }

    fun selectJob(jobId: String?) {
        _uiState.value = _uiState.value.copy(selectedJobId = jobId)
        if (jobId != null) {
            loadApplicationsForJob(jobId)
        }
    }

    private fun loadApplicationsForJob(jobId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Giriş yapmamış kullanıcı"
                    )
                    return@launch
                }
                
                // Önce job'u al, jobOwnerId'yi öğren
                val job = jobRepository.getJob(jobId)
                if (job == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "İş bulunamadı"
                    )
                    return@launch
                }
                
                // Eğer kullanıcı iş sahibiyse, getApplicationsByJobOwnerId kullan (daha güvenli)
                // Değilse, getApplicationsByJobId kullan (sadece kendi başvuruları)
                val applications = if (job.userId == currentUser.uid) {
                    // İş sahibi: Tüm başvuruları görebilir
                    jobApplicationRepository.getApplicationsByJobOwnerId(currentUser.uid)
                        .filter { it.jobId == jobId }
                } else {
                    // Başvuran: Sadece kendi başvurularını görebilir
                    jobApplicationRepository.getApplicationsByApplicantId(currentUser.uid)
                        .filter { it.jobId == jobId }
                }
                
                val applicantIds = applications.map { it.applicantId }.distinct()
                
                val applicationUsersMap = mutableMapOf<String, User>()
                applicantIds.forEach { applicantId ->
                    try {
                        userRepository.getUser(applicantId)?.let { user ->
                            applicationUsersMap[applicantId] = user
                        }
                    } catch (e: Exception) {
                        // Kullanıcı bilgisi yüklenemezse skip et ama hatayı logla
                        android.util.Log.e("CareerViewModel", "Kullanıcı bilgisi yüklenemedi: $applicantId - ${e.message}")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    applications = _uiState.value.applications + (jobId to applications),
                    applicationUsers = _uiState.value.applicationUsers + applicationUsersMap
                )
            } catch (t: Throwable) {
                android.util.Log.e("CareerViewModel", "Başvurular yüklenirken hata: ${t.message}", t)
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Başvurular yüklenirken hata oluştu"
                )
            }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                // Application'ı al (applicantId ve jobId için)
                val allApplications = jobApplicationRepository.getApplicationsByJobOwnerId(currentUser.uid)
                val application = allApplications.find { it.id == applicationId }
                
                if (application != null) {
                    // Status'u güncelle
                    jobApplicationRepository.updateApplicationStatus(applicationId, status)
                    
                    // Eğer status "accepted" ise, başvurana bildirim gönder
                    if (status == "accepted") {
                        try {
                            // İş bilgisini al
                            val job = jobRepository.getJob(application.jobId)
                            // İşveren bilgisini al
                            val employer = userRepository.getUser(currentUser.uid)
                            
                            if (job != null && employer != null) {
                                val notification = Notification(
                                    id = "",
                                    userId = application.applicantId, // Başvuran kullanıcı
                                    fromUserId = currentUser.uid, // İşveren
                                    type = "INVITE",
                                    relatedId = application.jobId,
                                    relatedType = "job",
                                    message = "${employer.name} ${employer.surname} accepted your application for ${job.title}",
                                    timestamp = System.currentTimeMillis(),
                                    isRead = false
                                )
                                notificationRepository.createNotification(notification)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CareerViewModel", "Bildirim oluşturulurken hata: ${e.message}")
                        }
                    }
                }
                
                // Seçili işin başvurularını yeniden yükle
                _uiState.value.selectedJobId?.let { jobId ->
                    loadApplicationsForJob(jobId)
                }
                
                // Başvuran sayılarını güncellemek için loadData'yı çağır
                loadData()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Başvuru durumu güncellenirken hata oluştu"
                )
            }
        }
    }

    fun removeRecommendedJob(jobId: String) {
        _uiState.value = _uiState.value.copy(
            recommendedJobs = _uiState.value.recommendedJobs.filter { it.id != jobId }
        )
    }

    fun refresh() {
        loadData()
    }
}

class CareerViewModelFactory(
    private val jobRepository: JobRepository = FirestoreJobRepository(),
    private val jobApplicationRepository: JobApplicationRepository = FirestoreJobApplicationRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareerViewModel::class.java)) {
            return CareerViewModel(jobRepository, jobApplicationRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
