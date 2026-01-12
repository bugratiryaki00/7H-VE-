package com.example.proto7hive.data

import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.Comment
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.MatchSuggestion
import com.example.proto7hive.model.Notification
import com.example.proto7hive.model.PortfolioCard
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.Project
import com.example.proto7hive.model.Role
import com.example.proto7hive.model.User

interface PortfolioRepository {
    suspend fun getPortfolios(): List<PortfolioCard>
}

interface ProjectRepository {
    suspend fun getProjects(): List<Project>
    suspend fun getProjectsByOwnerId(ownerId: String): List<Project> // Kullanıcının projeleri için
    suspend fun getRoles(projectId: String): List<Role>
}

interface AnnouncementRepository {
    suspend fun getAnnouncements(): List<Announcement>
}

interface MatchingRepository {
    suspend fun getSuggestions(forUserId: String): List<MatchSuggestion>
}

interface PostRepository {
    suspend fun getPosts(): List<Post>
    suspend fun getPostsByUserIds(userIds: List<String>): List<Post> // Bağlantıların postları için
    suspend fun getPostsByUserId(userId: String): List<Post> // Kullanıcının kendi postları için
    suspend fun createPost(post: Post): String // Yeni post oluştur (döner: postId)
    suspend fun searchPosts(query: String): List<Post> // İçerik metnine göre arama
    suspend fun likePost(postId: String, userId: String): Unit // Post'u beğen
    suspend fun unlikePost(postId: String, userId: String): Unit // Post beğenisini kaldır
    suspend fun getPost(postId: String): Post? // Tek bir post getir
}

interface UserRepository {
    suspend fun getUser(userId: String): User?
    suspend fun getUsers(userIds: List<String>): List<User> // Birden fazla kullanıcı bilgisi için
    suspend fun getAllUsers(): List<User> // Arama için tüm kullanıcıları getir
    suspend fun updateUser(user: User): Unit // Kullanıcı bilgilerini güncelle
    suspend fun searchUsers(query: String): List<User> // İsim veya email'e göre arama
}

interface ConnectionRepository {
    suspend fun getConnections(userId: String): List<String> // Kullanıcının bağlantı ID'leri
    suspend fun addConnection(userId: String, connectionUserId: String): Unit // Bağlantı ekle (karşılıklı)
    suspend fun removeConnection(userId: String, connectionUserId: String): Unit // Bağlantı çıkar (karşılıklı)
    suspend fun getConnectionUsers(userId: String): List<User> // Bağlantı yapılan kullanıcı bilgileri
    suspend fun getSuggestedConnections(userId: String): List<User> // Önerilen bağlantılar (basit: connections'ı olmayanlar)
    
    // Connection Request fonksiyonları
    suspend fun sendConnectionRequest(fromUserId: String, toUserId: String): String // İstek gönder (döner: requestId)
    suspend fun getPendingRequests(userId: String): List<com.example.proto7hive.model.ConnectionRequest> // Bekleyen istekleri getir (userId'ye gelen)
    suspend fun getSentRequests(userId: String): List<com.example.proto7hive.model.ConnectionRequest> // Gönderilen istekleri getir
    suspend fun acceptConnectionRequest(requestId: String): Unit // İsteği kabul et ve connection oluştur
    suspend fun rejectConnectionRequest(requestId: String): Unit // İsteği reddet
    suspend fun cancelConnectionRequest(requestId: String): Unit // Gönderilen isteği iptal et
}

interface JobRepository {
    suspend fun getJobs(): List<Job>
    suspend fun getJob(jobId: String): Job?
    suspend fun getJobsByUserId(userId: String): List<Job> // Kullanıcının paylaştığı işler
    suspend fun createJob(job: Job): String // Yeni iş ilanı oluştur
    suspend fun getSavedJobs(userId: String): List<String> // Kaydedilen iş ID'leri (users/{userId}/savedJobs subcollection)
    suspend fun saveJob(userId: String, jobId: String): Unit // İş kaydet
    suspend fun unsaveJob(userId: String, jobId: String): Unit // İş kaydını kaldır
    suspend fun searchJobs(query: String): List<Job> // Başlık, şirket veya açıklamaya göre arama
}

interface CommentRepository {
    suspend fun getCommentsByPostId(postId: String): List<Comment>
    suspend fun getCommentsByJobId(jobId: String): List<Comment>
    suspend fun createComment(comment: Comment): String // Yeni yorum oluştur (döner: commentId)
    suspend fun deleteComment(commentId: String): Unit // Yorum sil
}

interface NotificationRepository {
    suspend fun getNotifications(userId: String): List<Notification> // Tüm bildirimler
    suspend fun getNotificationsByType(userId: String, type: String): List<Notification> // Tip'e göre bildirimler
    suspend fun createNotification(notification: Notification): String // Yeni bildirim oluştur
    suspend fun markAsRead(notificationId: String): Unit // Bildirimi okundu olarak işaretle
    suspend fun markAllAsRead(userId: String): Unit // Tüm bildirimleri okundu olarak işaretle
}

interface JobApplicationRepository {
    suspend fun applyToJob(jobId: String, applicantId: String, jobOwnerId: String, message: String? = null): String // Başvuru oluştur (döner: applicationId)
    suspend fun getApplicationsByJobId(jobId: String): List<com.example.proto7hive.model.JobApplication> // Bir işe gelen başvurular
    suspend fun getApplicationsByApplicantId(applicantId: String): List<com.example.proto7hive.model.JobApplication> // Kullanıcının yaptığı başvurular
    suspend fun getApplicationsByJobOwnerId(jobOwnerId: String): List<com.example.proto7hive.model.JobApplication> // İşverenin işlerine gelen başvurular
    suspend fun updateApplicationStatus(applicationId: String, status: String): Unit // Status güncelle (pending -> accepted/rejected)
    suspend fun hasAppliedToJob(jobId: String, applicantId: String): Boolean // Kullanıcı bu işe başvurmuş mu?
}

interface CollectionRepository {
    suspend fun createCollection(collection: com.example.proto7hive.model.Collection): String // Yeni koleksiyon oluştur (döner: collectionId)
    suspend fun getCollectionsByUserId(userId: String): List<com.example.proto7hive.model.Collection> // Kullanıcının koleksiyonlarını getir
    suspend fun getCollection(collectionId: String): com.example.proto7hive.model.Collection? // Tek bir koleksiyon getir
    suspend fun updateCollection(collection: com.example.proto7hive.model.Collection): Unit // Koleksiyon güncelle
    suspend fun deleteCollection(collectionId: String): Unit // Koleksiyon sil
    suspend fun getJobsByCollectionId(collectionId: String): List<Job> // Koleksiyondaki işleri getir
}

class MockPortfolioRepository : PortfolioRepository {
    override suspend fun getPortfolios(): List<PortfolioCard> = MockData.portfolios
}

class MockProjectRepository : ProjectRepository {
    override suspend fun getProjects(): List<Project> = MockData.projects
    override suspend fun getProjectsByOwnerId(ownerId: String): List<Project> = 
        MockData.projects.filter { it.ownerId == ownerId }
    override suspend fun getRoles(projectId: String): List<Role> = MockData.roles.filter { it.projectId == projectId }
}

class MockAnnouncementRepository : AnnouncementRepository {
    override suspend fun getAnnouncements(): List<Announcement> = MockData.announcements
}

class MockMatchingRepository : MatchingRepository {
    override suspend fun getSuggestions(forUserId: String): List<MatchSuggestion> =
        MockData.suggestions.filter { it.userId == forUserId }.sortedByDescending { it.score }
}

object MockData {
    val users = listOf(
        User(id = "u1", name = "Ada", email = "ada@yeditepe.edu.tr", department = "CS", skills = listOf("Kotlin","Compose"), interests = listOf("Mobile"), availability = 80),
        User(id = "u2", name = "Deniz", email = "deniz@yeditepe.edu.tr", department = "Design", skills = listOf("Figma","UX"), interests = listOf("UI"), availability = 70),
    )

    val projects = listOf(
        Project(id = "p1", ownerId = "u1", title = "7hive App", description = "Campus network app", tags = listOf("Android","Compose")),
    )

    val roles = listOf(
        Role(id = "r1", projectId = "p1", title = "UI Designer", requiredSkills = listOf("Figma")),
    )

    val portfolios = listOf(
        PortfolioCard(userId = "u1", projects = projects, bio = "Android dev", imageUrl = null),
        PortfolioCard(userId = "u2", projects = emptyList(), bio = "Designer", imageUrl = null),
    )

    val announcements = listOf(
        Announcement(id = "a1", title = "Hackathon", body = "Weekend hackathon", dateIso = "2025-11-01", tags = listOf("event")),
    )

    val suggestions = listOf(
        MatchSuggestion(userId = "u1", suggestedUserId = "u2", score = 0.82),
    )
}


