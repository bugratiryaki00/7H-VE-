package com.example.proto7hive.data

import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.MatchSuggestion
import com.example.proto7hive.model.PortfolioCard
import com.example.proto7hive.model.Project
import com.example.proto7hive.model.Role
import com.example.proto7hive.model.User

interface PortfolioRepository {
    suspend fun getPortfolios(): List<PortfolioCard>
}

interface ProjectRepository {
    suspend fun getProjects(): List<Project>
    suspend fun getRoles(projectId: String): List<Role>
}

interface AnnouncementRepository {
    suspend fun getAnnouncements(): List<Announcement>
}

interface MatchingRepository {
    suspend fun getSuggestions(forUserId: String): List<MatchSuggestion>
}

class MockPortfolioRepository : PortfolioRepository {
    override suspend fun getPortfolios(): List<PortfolioCard> = MockData.portfolios
}

class MockProjectRepository : ProjectRepository {
    override suspend fun getProjects(): List<Project> = MockData.projects
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


