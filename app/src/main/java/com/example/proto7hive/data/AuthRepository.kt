package com.example.proto7hive.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val user: FirebaseUser? = null
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult(
                success = true,
                user = result.user,
                message = "Giriş başarılı"
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                message = e.message ?: "Giriş hatası"
            )
        }
    }

    suspend fun createUserWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            AuthResult(
                success = true,
                user = result.user,
                message = "Hesap oluşturuldu"
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                message = e.message ?: "Hesap oluşturma hatası"
            )
        }
    }

    suspend fun sendEmailVerification(): AuthResult {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                AuthResult(
                    success = true,
                    message = "Doğrulama e-postası gönderildi"
                )
            } else {
                AuthResult(
                    success = false,
                    message = "Kullanıcı bulunamadı"
                )
            }
        } catch (e: Exception) {
            AuthResult(
                success = false,
                message = e.message ?: "E-posta gönderme hatası"
            )
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false
}
