# Firebase Authentication ve Security Rules Kurulumu

## Yeni Değişiklikler İçin Kontrol Listesi ✅

### 1. Firebase Authentication - Email/Password Provider

**Kontrol:**
1. Firebase Console → Authentication → Sign-in method
2. **Email/Password** provider'ın **Enabled** olduğundan emin olun
3. Eğer kapalıysa, **Enable** butonuna tıklayın

**Önemli:** Email/Password provider kapalıysa, signup ve login çalışmaz!

---

### 2. Firestore Security Rules - Users Koleksiyonu

**Kontrol:**
1. Firebase Console → Firestore Database → Rules

**Şu Anki Durum:**
- Signup sırasında otomatik olarak `users/{userId}` dokümanı oluşturuluyor
- Kullanıcı kendi user dokümanını oluşturabilmeli

**Önerilen Security Rules:**

**Firebase Console'da Rules editörüne şunu kopyalayın (TAMAMINI, `rules_version` ile birlikte):**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users koleksiyonu - Kullanıcı kendi dokümanını oluşturabilir/güncelleyebilir
    match /users/{userId} {
      // Kullanıcı kendi dokümanını okuyabilir
      allow read: if request.auth != null;
      
      // Kullanıcı kendi dokümanını oluşturabilir (signup sırasında)
      allow create: if request.auth != null && request.auth.uid == userId;
      
      // Kullanıcı kendi dokümanını güncelleyebilir
      allow update: if request.auth != null && request.auth.uid == userId;
      
      // Kullanıcı kendi dokümanını silemez (güvenlik için)
      allow delete: if false;
    }
    
    // Saved Jobs subcollection
    match /users/{userId}/savedJobs/{jobId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Posts koleksiyonu - Giriş yapanlar okuyabilir, kendi postlarını oluşturabilir
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Jobs koleksiyonu - Giriş yapanlar okuyabilir, kendi işlerini oluşturabilir
    match /jobs/{jobId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Projects koleksiyonu
    match /projects/{projectId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.ownerId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.ownerId == request.auth.uid;
    }
  }
}
```

**Test İçin Basit Kurallar (Geçici - TAMAMINI kopyalayın):**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

**Not:** Firebase Console'da Rules editörü açıldığında, mevcut kodları silip yukarıdaki kodların TAMAMINI yapıştırın.

⚠️ **DİKKAT:** İkinci kural seti test için uygundur, production'da daha sıkı kurallar kullanılmalıdır!

---

### 3. Email Templates (Opsiyonel ama Önerilen)

**Password Reset Email:**
1. Firebase Console → Authentication → Templates
2. **Password reset** template'ini kontrol edin
3. İsterseniz email içeriğini özelleştirebilirsiniz (Türkçe, marka renkleri, vb.)

**Email Verification:**
1. Firebase Console → Authentication → Templates
2. **Email address verification** template'ini kontrol edin
3. İsterseniz email içeriğini özelleştirebilirsiniz

**Önemli:** Email template'leri otomatik olarak çalışır, özelleştirme opsiyoneldir.

---

### 4. Firebase Console'da Kontrol

**Signup Sonrası Kontrol:**
1. **Authentication → Users:** Yeni kullanıcı görünmeli
2. **Firestore → users koleksiyonu:** `users/{userId}` dokümanı otomatik oluşmuş olmalı

**Forgot Password Test:**
1. Login ekranından "I forgot my password" linkine tıklayın
2. Email adresini girin
3. Email'inize şifre sıfırlama linki gelmeli

---

## Checklist ✅

### Zorunlu:
- [ ] Email/Password provider **Enabled** ✅
- [ ] Firestore Security Rules güncellendi (users koleksiyonuna yazma izni var) ✅

### Opsiyonel (Önerilen):
- [ ] Email templates özelleştirildi (Türkçe, marka renkleri)
- [ ] Test signup yapıldı ve Firebase'de kontrol edildi
- [ ] Test forgot password yapıldı ve email geldi

---

## Hata Durumları

### Signup çalışmıyorsa:
1. Email/Password provider aktif mi kontrol edin
2. Firestore Rules'da `users/{userId}` için `create` izni var mı kontrol edin
3. Logcat'te hata mesajı var mı kontrol edin

### Forgot Password email gelmiyorsa:
1. Email adresinin doğru olduğundan emin olun
2. Spam klasörünü kontrol edin
3. Firebase Console → Authentication → Users → Kullanıcı var mı kontrol edin

### Users koleksiyonuna yazılmıyorsa:
1. Firestore Rules'da `users/{userId}` için `create` izni var mı kontrol edin
2. Kullanıcının giriş yapmış olduğundan emin olun (`request.auth != null`)
3. UID'nin doküman ID'si ile eşleştiğinden emin olun

---

## Güvenlik Notları

**Production'da:**
- Security Rules'ı mutlaka sıkılaştırın
- Kullanıcılar sadece kendi dokümanlarını okuyup yazabilsin
- Email verification'ı zorunlu kılabilirsiniz (Firebase Console → Authentication → Settings)
- Rate limiting ekleyin (Firebase Extensions veya Cloud Functions ile)

**Test Ortamı:**
- Basit kurallar kullanabilirsiniz ama production'a geçmeden önce mutlaka güncelleyin

