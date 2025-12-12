package com.example.proto7hive.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
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

@Serializable
data class Post(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L // Unix timestamp (milisaniye)
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
    val userId: String = "" // İşi paylaşan kullanıcı ID'si (işveren)
)

@Serializable
data class MatchSuggestion(
    val userId: String = "",
    val suggestedUserId: String = "",
    @SerialName("score") val score: Double = 0.0
)


