package com.example.proto7hive.data

import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.Project
import com.example.proto7hive.model.PortfolioCard
import com.example.proto7hive.model.MatchSuggestion
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.User
import com.example.proto7hive.model.Job
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await

class FirestoreProjectRepository(
	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProjectRepository {
	override suspend fun getProjects(): List<Project> {
		val snap = db.collection("projects").get().await()
		return snap.documents.mapNotNull { doc ->
			doc.toObject(Project::class.java)?.copy(id = doc.id)
		}
	}

	override suspend fun getProjectsByOwnerId(ownerId: String): List<Project> {
		val snap = db.collection("projects")
			.whereEqualTo("ownerId", ownerId)
			.get().await()
		return snap.documents.mapNotNull { doc ->
			doc.toObject(Project::class.java)?.copy(id = doc.id)
		}
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

class FirestorePostRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : com.example.proto7hive.data.PostRepository {
    override suspend fun getPosts(): List<Post> {
        val snap = db.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getPostsByUserIds(userIds: List<String>): List<Post> {
        if (userIds.isEmpty()) return emptyList()
        
        // Firestore'da 'in' query ile en fazla 10 ID'ye kadar sorgu yapabiliriz
        // Daha fazlası için batch queries gerekir
        val batches = userIds.chunked(10)
        val allPosts = mutableListOf<Post>()
        
        for (batch in batches) {
            val snap = db.collection("posts")
                .whereIn("userId", batch)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            allPosts.addAll(snap.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            })
        }
        
        return allPosts.sortedByDescending { it.timestamp }
    }

    override suspend fun getPostsByUserId(userId: String): List<Post> {
        val snap = db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun createPost(post: Post): String {
        val docRef = db.collection("posts").document()
        val postWithId = post.copy(id = docRef.id)
        docRef.set(postWithId).await()
        return docRef.id
    }
}

class FirestoreUserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {
    override suspend fun getUser(userId: String): User? {
        val doc = db.collection("users").document(userId).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)
    }

    override suspend fun getUsers(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        val batches = userIds.chunked(10)
        val allUsers = mutableListOf<User>()
        
        for (batch in batches) {
            val snap = db.collection("users").whereIn(FieldPath.documentId(), batch).get().await()
            allUsers.addAll(snap.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            })
        }
        
        return allUsers
    }

    override suspend fun getAllUsers(): List<User> {
        val snap = db.collection("users").get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun updateUser(user: User) {
        db.collection("users").document(user.id).set(user).await()
    }

    override suspend fun searchUsers(query: String): List<User> {
        val lowerQuery = query.lowercase()
        val snap = db.collection("users").get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }.filter { user ->
            user.name.lowercase().contains(lowerQuery) || 
            user.email.lowercase().contains(lowerQuery)
        }
    }
}

class FirestoreConnectionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ConnectionRepository {
    override suspend fun getConnections(userId: String): List<String> {
        val user = db.collection("users").document(userId).get().await()
        val userObj = user.toObject(User::class.java)
        return userObj?.connections ?: emptyList()
    }

    override suspend fun addConnection(userId: String, connectionUserId: String) {
        db.collection("users").document(userId)
            .update("connections", FieldValue.arrayUnion(connectionUserId)).await()
    }

    override suspend fun removeConnection(userId: String, connectionUserId: String) {
        db.collection("users").document(userId)
            .update("connections", FieldValue.arrayRemove(connectionUserId)).await()
    }

    override suspend fun getConnectionUsers(userId: String): List<User> {
        val connectionIds = getConnections(userId)
        if (connectionIds.isEmpty()) return emptyList()
        return FirestoreUserRepository(db).getUsers(connectionIds)
    }

    override suspend fun getSuggestedConnections(userId: String): List<User> {
        val currentConnections = getConnections(userId)
        val allUsers = FirestoreUserRepository(db).getAllUsers()
        // Basit öneri: bağlantısı olmayanlar (ileride daha gelişmiş algoritma)
        return allUsers.filter { it.id != userId && it.id !in currentConnections }
    }
}

class FirestoreJobRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : JobRepository {
    override suspend fun getJobs(): List<Job> {
        val snap = db.collection("jobs").get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Job::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getJob(jobId: String): Job? {
        val doc = db.collection("jobs").document(jobId).get().await()
        return doc.toObject(Job::class.java)?.copy(id = doc.id)
    }

    override suspend fun getJobsByUserId(userId: String): List<Job> {
        val snap = db.collection("jobs").whereEqualTo("userId", userId).get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Job::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun createJob(job: Job): String {
        val docRef = db.collection("jobs").document()
        val jobWithId = job.copy(id = docRef.id)
        docRef.set(jobWithId).await()
        return docRef.id
    }

    override suspend fun getSavedJobs(userId: String): List<String> {
        val snap = db.collection("users").document(userId)
            .collection("savedJobs").get().await()
        return snap.documents.map { it.id }
    }

    override suspend fun saveJob(userId: String, jobId: String) {
        db.collection("users").document(userId)
            .collection("savedJobs").document(jobId).set(mapOf("savedAt" to System.currentTimeMillis())).await()
    }

    override suspend fun unsaveJob(userId: String, jobId: String) {
        db.collection("users").document(userId)
            .collection("savedJobs").document(jobId).delete().await()
    }
}
