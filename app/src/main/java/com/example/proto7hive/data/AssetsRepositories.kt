package com.example.proto7hive.data

import android.content.res.AssetManager
import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.MatchSuggestion
import com.example.proto7hive.model.PortfolioCard
import com.example.proto7hive.model.Project
import com.example.proto7hive.model.Role
import com.example.proto7hive.model.User
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class AssetsPortfolioRepository(private val assets: AssetManager) : PortfolioRepository {
    override suspend fun getPortfolios(): List<PortfolioCard> {
        val text = assets.open("portfolios.json").use { it.readBytes().decodeToString() }
        return json.decodeFromString(ListSerializer(PortfolioCard.serializer()), text)
    }
}

class AssetsProjectRepository(private val assets: AssetManager) : ProjectRepository {
    override suspend fun getProjects(): List<Project> {
        val text = assets.open("projects.json").use { it.readBytes().decodeToString() }
        return json.decodeFromString(ListSerializer(Project.serializer()), text)
    }

    override suspend fun getRoles(projectId: String): List<Role> {
        val text = assets.open("roles.json").use { it.readBytes().decodeToString() }
        val all = json.decodeFromString(ListSerializer(Role.serializer()), text)
        return all.filter { it.projectId == projectId }
    }
}

class AssetsAnnouncementRepository(private val assets: AssetManager) : AnnouncementRepository {
    override suspend fun getAnnouncements(): List<Announcement> {
        val text = assets.open("announcements.json").use { it.readBytes().decodeToString() }
        return json.decodeFromString(ListSerializer(Announcement.serializer()), text)
    }
}

class AssetsMatchingRepository(private val assets: AssetManager) : MatchingRepository {
    override suspend fun getSuggestions(forUserId: String): List<MatchSuggestion> {
        val text = assets.open("matches.json").use { it.readBytes().decodeToString() }
        val all = json.decodeFromString(ListSerializer(MatchSuggestion.serializer()), text)
        return all.filter { it.userId == forUserId }.sortedByDescending { it.score }
    }
}

class AssetsUserRepository(private val assets: AssetManager) {
    suspend fun getUsers(): List<User> {
        val text = assets.open("users.json").use { it.readBytes().decodeToString() }
        return json.decodeFromString(ListSerializer(User.serializer()), text)
    }
}


