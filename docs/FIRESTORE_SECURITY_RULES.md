# Firestore Security Rules

## Güncel Kurallar (Projects Kaldırıldı ✅)

Firebase Console → Firestore Database → Rules sekmesine şunu yapıştırın:

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
    
    // Comments koleksiyonu - Post ve Job'lar için yorumlar
    match /comments/{commentId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```

## Koleksiyonlar

- ✅ `users` - Kullanıcı bilgileri
- ✅ `posts` - Post'lar (postType: "post" veya "work")
- ✅ `jobs` - İş ilanları
- ✅ `comments` - Yorumlar (post ve job'lar için)
- ❌ `projects` - **Kaldırıldı** (artık kullanılmıyor)

## Güvenlik Açıklaması

- **read**: Tüm giriş yapan kullanıcılar okuyabilir
- **create**: Kullanıcı sadece kendi dokümanını oluşturabilir (`userId` kontrolü ile)
- **update/delete**: Kullanıcı sadece kendi dokümanını güncelleyebilir/silebilir
- **users delete**: Kullanıcı kendi user dokümanını silemez (güvenlik için)

## Test İçin Basit Kurallar (Geçici - Güvenlik Açığı Var!)

**SADECE TEST İÇİN KULLANIN, PRODUCTION'DA KULLANMAYIN!**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

**Not:** Firebase Console'da Rules'ı yapıştırdıktan sonra mutlaka **Publish** butonuna tıklayın!

