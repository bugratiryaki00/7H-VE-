package com.example.proto7hive.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadPostImage(imageUri: Uri): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Kullanıcı giriş yapmamış")
        
        // Benzersiz dosya adı oluştur: posts/{userId}/{uuid}.jpg
        val fileName = "posts/${currentUser.uid}/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)
        
        // Görseli yükle
        val uploadTask = storageRef.putFile(imageUri).await()
        
        // Download URL'ini al
        val downloadUrl = uploadTask.storage.downloadUrl.await()
        
        return downloadUrl.toString()
    }
}

