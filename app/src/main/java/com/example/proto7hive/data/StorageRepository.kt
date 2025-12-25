package com.example.proto7hive.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
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
        
        Log.d("StorageRepository", "Görsel yükleniyor: $fileName, URI: $imageUri")
        
        try {
            // Görseli yükle
            val uploadTask = storageRef.putFile(imageUri)
            uploadTask.await()
            
            Log.d("StorageRepository", "Görsel başarıyla yüklendi: $fileName")
            
            // Download URL'ini al
            val downloadUrl = storageRef.downloadUrl.await()
            
            Log.d("StorageRepository", "Download URL alındı: $downloadUrl")
            
            return downloadUrl.toString()
        } catch (e: StorageException) {
            Log.e("StorageRepository", "Storage hatası: code=${e.errorCode}, message=${e.message}")
            val errorMessage = when (e.errorCode) {
                StorageException.ERROR_OBJECT_NOT_FOUND -> {
                    "Firebase Storage başlatılmamış. Firebase Console → Storage → Get Started"
                }
                -13021 -> { // ERROR_UNAUTHORIZED için sayısal değer
                    "Yetki hatası: Storage Security Rules kontrol edin"
                }
                else -> {
                    "Görsel yükleme hatası: ${e.message}"
                }
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            Log.e("StorageRepository", "Görsel yükleme hatası", e)
            throw Exception("Görsel yüklenemedi: ${e.message}")
        }
    }

    suspend fun uploadProfileImage(imageUri: Uri): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Kullanıcı giriş yapmamış")
        
        Log.d("StorageRepository", "uploadProfileImage başladı, userId: ${currentUser.uid}, URI: $imageUri")
        
        // Benzersiz dosya adı oluştur: profiles/{userId}/{uuid}.jpg
        val fileName = "profiles/${currentUser.uid}/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)
        
        Log.d("StorageRepository", "Profil görseli yükleniyor: $fileName")
        Log.d("StorageRepository", "Storage reference path: ${storageRef.path}")
        
        try {
            // Görseli yükle
            Log.d("StorageRepository", "putFile çağrılıyor...")
            val uploadTask = storageRef.putFile(imageUri)
            
            Log.d("StorageRepository", "Upload task await ediliyor...")
            val taskSnapshot = uploadTask.await()
            Log.d("StorageRepository", "Upload tamamlandı: ${taskSnapshot.metadata?.sizeBytes} bytes")
            
            // Download URL'ini al
            Log.d("StorageRepository", "Download URL alınıyor...")
            val downloadUrl = storageRef.downloadUrl.await()
            
            Log.d("StorageRepository", "Profil görseli başarıyla yüklendi: $fileName")
            Log.d("StorageRepository", "Download URL: $downloadUrl")
            
            return downloadUrl.toString()
        } catch (e: StorageException) {
            Log.e("StorageRepository", "StorageException yakalandı", e)
            Log.e("StorageRepository", "Error code: ${e.errorCode}, Error message: ${e.message}")
            Log.e("StorageRepository", "HTTP result: ${e.httpResultCode}")
            
            val errorMessage = when (e.errorCode) {
                StorageException.ERROR_OBJECT_NOT_FOUND -> {
                    "Firebase Storage başlatılmamış. Firebase Console → Storage → Get Started"
                }
                -13021 -> { // ERROR_UNAUTHORIZED için sayısal değer
                    "Yetki hatası: Storage Security Rules kontrol edin. profiles/ klasörü için write izni gerekiyor."
                }
                else -> {
                    "Profil görseli yükleme hatası (kod: ${e.errorCode}): ${e.message}"
                }
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            Log.e("StorageRepository", "Profil görseli yükleme hatası", e)
            Log.e("StorageRepository", "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
            e.printStackTrace()
            throw Exception("Profil görseli yüklenemedi: ${e.message}")
        }
    }
}

