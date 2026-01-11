package com.example.proto7hive.data

import com.example.proto7hive.model.Announcement
import com.example.proto7hive.model.Comment
import com.example.proto7hive.model.Project
import com.example.proto7hive.model.PortfolioCard
import com.example.proto7hive.model.MatchSuggestion
import com.example.proto7hive.model.Notification
import com.example.proto7hive.model.Post
import com.example.proto7hive.model.User
import com.example.proto7hive.model.Job
import com.example.proto7hive.model.ConnectionRequest
import com.example.proto7hive.model.JobApplication
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

    override suspend fun searchPosts(query: String): List<Post> {
        val lowerQuery = query.lowercase()
        val snap = db.collection("posts").get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }.filter { post ->
            post.text.lowercase().contains(lowerQuery)
        }.sortedByDescending { it.timestamp }
    }

    override suspend fun getPost(postId: String): Post? {
        val doc = db.collection("posts").document(postId).get().await()
        return doc.toObject(Post::class.java)?.copy(id = doc.id)
    }

    override suspend fun likePost(postId: String, userId: String) {
        db.collection("posts").document(postId)
            .update("likes", FieldValue.arrayUnion(userId)).await()
    }

    override suspend fun unlikePost(postId: String, userId: String) {
        db.collection("posts").document(postId)
            .update("likes", FieldValue.arrayRemove(userId)).await()
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
            user.surname.lowercase().contains(lowerQuery) ||
            (user.name.lowercase() + " " + user.surname.lowercase()).contains(lowerQuery) ||
            user.email.lowercase().contains(lowerQuery) ||
            (user.department?.lowercase()?.contains(lowerQuery) == true)
        }
    }
}

class FirestoreConnectionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ConnectionRepository {
    override suspend fun getConnections(userId: String): List<String> {
        val user = db.collection("users").document(userId).get().await()
        val userObj = user.toObject(User::class.java)
        val directConnections = userObj?.connections ?: emptyList()
        
        // Accepted connection request'leri de connections olarak ekle
        // Ancak sadece kullanıcının kendi request'leri okunabilir (security rules nedeniyle)
        // Başka bir kullanıcının profilini görüntülerken permission denied alabiliriz
        val acceptedToUserIds = try {
            // Gönderilen accepted request'ler (fromUserId == userId, status == accepted)
            val sentAcceptedRequests = db.collection("connectionRequests")
                .whereEqualTo("fromUserId", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()
            sentAcceptedRequests.documents.mapNotNull { doc ->
                doc.data?.get("toUserId") as? String
            }
        } catch (e: Exception) {
            // Permission denied veya başka bir hata - sadece direkt connections'ı kullan
            emptyList()
        }
        
        val acceptedFromUserIds = try {
            // Gelen accepted request'ler (toUserId == userId, status == accepted)
            val receivedAcceptedRequests = db.collection("connectionRequests")
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()
            receivedAcceptedRequests.documents.mapNotNull { doc ->
                doc.data?.get("fromUserId") as? String
            }
        } catch (e: Exception) {
            // Permission denied veya başka bir hata - sadece direkt connections'ı kullan
            emptyList()
        }
        
        // Tüm connections'ları birleştir (duplicate'leri kaldır)
        return (directConnections + acceptedToUserIds + acceptedFromUserIds).distinct()
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
        val sentRequests = getSentRequests(userId).map { it.toUserId }
        val pendingRequests = getPendingRequests(userId).map { it.fromUserId }
        val allUsers = FirestoreUserRepository(db).getAllUsers()
        // Basit öneri: bağlantısı olmayanlar ve istek gönderilmemiş olanlar
        return allUsers.filter { 
            it.id != userId && 
            it.id !in currentConnections &&
            it.id !in sentRequests &&
            it.id !in pendingRequests
        }
    }

    override suspend fun sendConnectionRequest(fromUserId: String, toUserId: String): String {
        val doc = db.collection("connectionRequests").document()
        val data = hashMapOf(
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        doc.set(data).await()
        return doc.id
    }

    override suspend fun getPendingRequests(userId: String): List<ConnectionRequest> {
        val snap = db.collection("connectionRequests")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            ConnectionRequest(
                id = doc.id,
                fromUserId = data?.get("fromUserId") as? String ?: "",
                toUserId = data?.get("toUserId") as? String ?: "",
                status = data?.get("status") as? String ?: "pending",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L
            )
        }
    }

    override suspend fun getSentRequests(userId: String): List<ConnectionRequest> {
        val snap = db.collection("connectionRequests")
            .whereEqualTo("fromUserId", userId)
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            ConnectionRequest(
                id = doc.id,
                fromUserId = data?.get("fromUserId") as? String ?: "",
                toUserId = data?.get("toUserId") as? String ?: "",
                status = data?.get("status") as? String ?: "pending",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L
            )
        }
    }

    override suspend fun acceptConnectionRequest(requestId: String) {
        val request = db.collection("connectionRequests").document(requestId).get().await()
        val data = request.data
        val fromUserId = data?.get("fromUserId") as? String ?: ""
        val toUserId = data?.get("toUserId") as? String ?: ""
        
        if (fromUserId.isNotEmpty() && toUserId.isNotEmpty()) {
            // İsteği accepted olarak işaretle
            db.collection("connectionRequests").document(requestId)
                .update("status", "accepted").await()
            
            // Batch write kullanarak her iki kullanıcının da connections listesini güncelle
            // Not: Batch write'lar her işlem için ayrı security check yapar
            // Bu yüzden sadece currentUser'ın (toUserId) kendi connections listesine ekleme yapabiliriz
            // fromUserId'nin connections listesine ekleme yapamayız çünkü permission denied alırız
            
            // Sadece kabul eden kullanıcının (toUserId) connections listesine gönderen kullanıcıyı (fromUserId) ekle
            addConnection(toUserId, fromUserId)
            
            // Gönderen kullanıcının (fromUserId) connections listesine kabul eden kullanıcı (toUserId) eklenemez
            // çünkü currentUser (toUserId) başka bir kullanıcının (fromUserId) dokümanını güncelleyemez
            // Bu işlem, fromUserId kullanıcısı profil yüklendiğinde veya başka bir şekilde güncellendiğinde yapılabilir
        }
    }

    override suspend fun rejectConnectionRequest(requestId: String) {
        db.collection("connectionRequests").document(requestId)
            .update("status", "rejected").await()
    }

    override suspend fun cancelConnectionRequest(requestId: String) {
        db.collection("connectionRequests").document(requestId)
            .delete().await()
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

    override suspend fun searchJobs(query: String): List<Job> {
        val lowerQuery = query.lowercase()
        val snap = db.collection("jobs").get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Job::class.java)?.copy(id = doc.id)
        }.filter { job ->
            job.title.lowercase().contains(lowerQuery) ||
            job.company.lowercase().contains(lowerQuery) ||
            job.description.lowercase().contains(lowerQuery) ||
            job.location.lowercase().contains(lowerQuery)
        }
    }
}

class FirestoreCommentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CommentRepository {
    
    override suspend fun getCommentsByPostId(postId: String): List<Comment> {
        val snapshot = db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Comment::class.java)?.copy(id = doc.id)
        }
    }
    
    override suspend fun getCommentsByJobId(jobId: String): List<Comment> {
        val snapshot = db.collection("comments")
            .whereEqualTo("jobId", jobId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Comment::class.java)?.copy(id = doc.id)
        }
    }
    
    override suspend fun createComment(comment: Comment): String {
        val doc = db.collection("comments").document()
        val data = hashMapOf(
            "postId" to (comment.postId ?: ""),
            "jobId" to (comment.jobId ?: ""),
            "userId" to comment.userId,
            "text" to comment.text,
            "timestamp" to comment.timestamp
        )
        doc.set(data).await()
        return doc.id
    }
    
    override suspend fun deleteComment(commentId: String) {
        db.collection("comments").document(commentId).delete().await()
    }
}

class FirestoreNotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepository {
    override suspend fun getNotifications(userId: String): List<Notification> {
        val snap = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            Notification(
                id = doc.id,
                userId = data?.get("userId") as? String ?: "",
                fromUserId = data?.get("fromUserId") as? String ?: "",
                type = data?.get("type") as? String ?: "",
                relatedId = data?.get("relatedId") as? String,
                relatedType = data?.get("relatedType") as? String,
                message = data?.get("message") as? String ?: "",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L,
                isRead = (data?.get("isRead") as? Boolean) ?: false
            )
        }
    }

    override suspend fun getNotificationsByType(userId: String, type: String): List<Notification> {
        val snap = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            Notification(
                id = doc.id,
                userId = data?.get("userId") as? String ?: "",
                fromUserId = data?.get("fromUserId") as? String ?: "",
                type = data?.get("type") as? String ?: "",
                relatedId = data?.get("relatedId") as? String,
                relatedType = data?.get("relatedType") as? String,
                message = data?.get("message") as? String ?: "",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L,
                isRead = (data?.get("isRead") as? Boolean) ?: false
            )
        }
    }

    override suspend fun createNotification(notification: Notification): String {
        val doc = db.collection("notifications").document()
        val data = hashMapOf(
            "userId" to notification.userId,
            "fromUserId" to notification.fromUserId,
            "type" to notification.type,
            "relatedId" to (notification.relatedId ?: ""),
            "relatedType" to (notification.relatedType ?: ""),
            "message" to notification.message,
            "timestamp" to notification.timestamp,
            "isRead" to notification.isRead
        )
        doc.set(data).await()
        return doc.id
    }

    override suspend fun markAsRead(notificationId: String) {
        db.collection("notifications").document(notificationId)
            .update("isRead", true)
            .await()
    }

    override suspend fun markAllAsRead(userId: String) {
        val snap = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
        
        val batch = db.batch()
        snap.documents.forEach { doc ->
            batch.update(doc.reference, "isRead", true)
        }
        batch.commit().await()
    }
}

class FirestoreJobApplicationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : JobApplicationRepository {
    override suspend fun applyToJob(jobId: String, applicantId: String, jobOwnerId: String, message: String?): String {
        val docRef = db.collection("jobApplications").document()
        val application = JobApplication(
            id = docRef.id,
            jobId = jobId,
            applicantId = applicantId,
            jobOwnerId = jobOwnerId,
            status = "pending",
            timestamp = System.currentTimeMillis(),
            message = message
        )
        val data = hashMapOf(
            "jobId" to application.jobId,
            "applicantId" to application.applicantId,
            "jobOwnerId" to application.jobOwnerId,
            "status" to application.status,
            "timestamp" to application.timestamp,
            "message" to (application.message ?: "")
        )
        docRef.set(data).await()
        return docRef.id
    }

    override suspend fun getApplicationsByJobId(jobId: String): List<JobApplication> {
        val snap = db.collection("jobApplications")
            .whereEqualTo("jobId", jobId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            JobApplication(
                id = doc.id,
                jobId = data?.get("jobId") as? String ?: "",
                applicantId = data?.get("applicantId") as? String ?: "",
                jobOwnerId = data?.get("jobOwnerId") as? String ?: "",
                status = data?.get("status") as? String ?: "pending",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L,
                message = data?.get("message") as? String
            )
        }
    }

    override suspend fun getApplicationsByApplicantId(applicantId: String): List<JobApplication> {
        val snap = db.collection("jobApplications")
            .whereEqualTo("applicantId", applicantId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            JobApplication(
                id = doc.id,
                jobId = data?.get("jobId") as? String ?: "",
                applicantId = data?.get("applicantId") as? String ?: "",
                jobOwnerId = data?.get("jobOwnerId") as? String ?: "",
                status = data?.get("status") as? String ?: "pending",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L,
                message = data?.get("message") as? String
            )
        }
    }

    override suspend fun getApplicationsByJobOwnerId(jobOwnerId: String): List<JobApplication> {
        val snap = db.collection("jobApplications")
            .whereEqualTo("jobOwnerId", jobOwnerId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data
            JobApplication(
                id = doc.id,
                jobId = data?.get("jobId") as? String ?: "",
                applicantId = data?.get("applicantId") as? String ?: "",
                jobOwnerId = data?.get("jobOwnerId") as? String ?: "",
                status = data?.get("status") as? String ?: "pending",
                timestamp = (data?.get("timestamp") as? Long) ?: 0L,
                message = data?.get("message") as? String
            )
        }
    }

    override suspend fun updateApplicationStatus(applicationId: String, status: String) {
        db.collection("jobApplications").document(applicationId)
            .update("status", status)
            .await()
    }

    override suspend fun hasAppliedToJob(jobId: String, applicantId: String): Boolean {
        val snap = db.collection("jobApplications")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("applicantId", applicantId)
            .limit(1)
            .get()
            .await()
        return !snap.isEmpty
    }
}
