package com.example.proto7hive.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val department: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val badges: List<String> = emptyList(),
    val availability: Int? = null // 0-100 arası uygunluk skoru
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
    val requiredSkills: List<String> = emptyList(),
    val level: String? = null // junior/mid/senior benzeri basit etiket
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
data class MatchSuggestion(
    val userId: String = "",
    val suggestedUserId: String = "",
    @SerialName("score") val score: Double = 0.0
)


