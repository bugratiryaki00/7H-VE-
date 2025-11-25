package com.example.proto7hive.data

import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.Project
import com.example.proto7hive.model.PortfolioCard
import com.example.proto7hive.model.MatchSuggestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProjectRepository(
	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProjectRepository {
	override suspend fun getProjects(): List<Project> {
		val snap = db.collection("projects").get().await()
		return snap.documents.mapNotNull { it.toObject(Project::class.java) }
	}

	override suspend fun getRoles(projectId: String) = AssetsProjectRepository(
		// roles için basit yaklaşım: assets'ten okumaya devam (istersen Firestore'a taşınır)
		// ileride Firestore'a taşıyacağız
		assets = throw UnsupportedOperationException("Provide Assets or implement Firestore roles")
	).getRoles(projectId)
}

class FirestoreAnnouncementRepository(
	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AnnouncementRepository {
	override suspend fun getAnnouncements(): List<Announcement> {
		val snap = db.collection("announcements").get().await()
		return snap.documents.mapNotNull { it.toObject(Announcement::class.java) }
	}
}

class FirestorePortfolioRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PortfolioRepository {
    override suspend fun getPortfolios(): List<PortfolioCard> {
        val snap = db.collection("portfolios").get().await()
        return snap.documents.mapNotNull { it.toObject(PortfolioCard::class.java) }
    }
}

class FirestoreMatchingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MatchingRepository {
    override suspend fun getSuggestions(forUserId: String): List<MatchSuggestion> {
        val snap = db.collection("matches").whereEqualTo("userId", forUserId).get().await()
        return snap.documents.mapNotNull { it.toObject(MatchSuggestion::class.java) }
            .sortedByDescending { it.score }
    }
}
