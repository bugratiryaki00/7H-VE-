package com.example.proto7hive.data

import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.Comment
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.MatchSuggestion
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
    suspend fun addConnection(userId: String, connectionUserId: String): Unit // Bağlantı ekle
    suspend fun removeConnection(userId: String, connectionUserId: String): Unit // Bağlantı çıkar
    suspend fun getConnectionUsers(userId: String): List<User> // Bağlantı yapılan kullanıcı bilgileri
    suspend fun getSuggestedConnections(userId: String): List<User> // Önerilen bağlantılar (basit: connections'ı olmayanlar)
}

interface JobRepository {
    suspend fun getJobs(): List<Job>
    suspend fun getJob(jobId: String): Job?
    suspend fun getJobsByUserId(userId: String): List<Job> // Kullanıcının paylaştığı işler
    suspend fun createJob(job: Job): String // Yeni iş ilanı oluştur
    suspend fun getSavedJobs(userId: String): List<String> // Kaydedilen iş ID'leri (users/{userId}/savedJobs subcollection)
    suspend fun saveJob(userId: String, jobId: String): Unit // İş kaydet
    suspend fun unsaveJob(userId: String, jobId: String): Unit // İş kaydını kaldır
}

interface CommentRepository {
    suspend fun getCommentsByPostId(postId: String): List<Comment>
    suspend fun getCommentsByJobId(jobId: String): List<Comment>
    suspend fun createComment(comment: Comment): String // Yeni yorum oluştur (döner: commentId)
    suspend fun deleteComment(commentId: String): Unit // Yorum sil
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


