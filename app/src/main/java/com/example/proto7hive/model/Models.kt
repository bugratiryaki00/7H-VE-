package com.example.proto7hive.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val userType: String? = null, // Student, Staff, Academician, Graduate
    val gender: String? = null, // Male, Female, The Other, I don't want to specify
    val dateOfBirth: Long? = null, // Unix timestamp (milisaniye)
    val department: String? = null, //Should
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val badges: List<String> = emptyList(),
    val availability: Int? = null, // 0-100 arası uygunluk skoru
    val connections: List<String> = emptyList(), // Bağlantı yapılan kullanıcı ID'leri
    val profileImageUrl: String? = null,
    val bio: String? = null
)

@Serializable
data class Project(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null
)

@Serializable
data class Role(
    val id: String = "",
    val projectId: String = "",
    val title: String = "",
    val requiredSkills: List<String> = emptyList(), //Should
    val level: String? = null 
)

@Serializable
data class PortfolioCard(
    val userId: String = "",
    val projects: List<Project> = emptyList(),
    val bio: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val dateIso: String = "",
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null
)

// @Serializable removed - Firestore uses Java serialization
data class Post(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L, // Unix timestamp (milisaniye)
    val postType: String = "post", // "post" veya "work"
    val likes: List<String> = emptyList() // Beğenen kullanıcı ID'leri
)

@Serializable
data class Job(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val workType: String = "", // Full-time, Part-time, Remote, Hybrid, On-site
    val description: String = "",
    val requiredSkills: List<String> = emptyList(),
    val imageUrl: String? = null,
    val userId: String = "", // İşi paylaşan kullanıcı ID'si (işveren)
    val collectionId: String? = null, // Koleksiyon ID'si (work koleksiyonuna ait)
    val isJobPosting: Boolean = true // true = çalışan arıyor, false = kişisel iş/portfolio
)

@Serializable
data class MatchSuggestion(
    val userId: String = "",
    val suggestedUserId: String = "",
    @SerialName("score") val score: Double = 0.0
)

@Serializable
data class Comment(
    val id: String = "",
    val postId: String? = null, // Post ID'si (post için)
    val jobId: String? = null, // Job ID'si (work için)
    val userId: String = "", // Yorum yapan kullanıcı ID'si
    val text: String = "", // Yorum metni
    val timestamp: Long = 0L // Unix timestamp (milisaniye)
)

@Serializable
data class Notification(
    val id: String = "",
    val userId: String = "", // Bildirimi alan kullanıcı
    val fromUserId: String = "", // Bildirimi gönderen kullanıcı
    val type: String = "", // COMMENT, FOLLOW_REQUEST, INVITE
    val relatedId: String? = null, // postId, jobId, projectId, vs.
    val relatedType: String? = null, // post, job, project, team
    val message: String = "", // Bildirim mesajı
    val timestamp: Long = 0L, // Unix timestamp (milisaniye)
    val isRead: Boolean = false
)

@Serializable
data class ConnectionRequest(
    val id: String = "",
    val fromUserId: String = "", // İsteği gönderen kullanıcı
    val toUserId: String = "", // İsteği alan kullanıcı
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = 0L // Unix timestamp (milisaniye)
)

@Serializable
data class JobApplication(
    val id: String = "",
    val jobId: String = "", // Başvurulan iş ID'si
    val applicantId: String = "", // Başvuran kullanıcı ID'si
    val jobOwnerId: String = "", // İş sahibi (job.userId)
    val status: String = "pending", // "pending", "accepted", "rejected"
    val timestamp: Long = 0L, // Unix timestamp (milisaniye)
    val message: String? = null // Opsiyonel cover letter
)

@Serializable
data class Collection(
    val id: String = "",
    val name: String = "",
    val userId: String = "", // Koleksiyon sahibi
    val thumbnailUrl: String? = null, // Koleksiyon görseli
    val createdAt: Long = 0L // Unix timestamp (milisaniye)
)


