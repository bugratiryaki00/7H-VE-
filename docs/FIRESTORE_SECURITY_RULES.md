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
      // Post sahibi tüm field'ları güncelleyebilir/silebilir
      // Herkes sadece likes field'ını güncelleyebilir (beğeni için)
      allow update: if request.auth != null && (
        resource.data.userId == request.auth.uid ||  // Post sahibi her şeyi güncelleyebilir
        (request.resource.data.userId == resource.data.userId &&  // userId değiştirilemez
         request.resource.data.text == resource.data.text &&  // text değiştirilemez
         request.resource.data.imageUrl == resource.data.imageUrl &&  // imageUrl değiştirilemez
         request.resource.data.timestamp == resource.data.timestamp &&  // timestamp değiştirilemez
         request.resource.data.postType == resource.data.postType)  // postType değiştirilemez (sadece likes güncellenebilir)
      );
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;
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
    
    // Notifications koleksiyonu - Kullanıcılar kendi bildirimlerini okuyabilir/güncelleyebilir
    match /notifications/{notificationId} {
      // Kullanıcı kendi bildirimlerini okuyabilir (userId kontrolü ile)
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      // Herkes bildirim oluşturabilir (başkaları kullanıcıya bildirim gönderebilir)
      allow create: if request.auth != null;
      // Kullanıcı kendi bildirimlerini güncelleyebilir (okundu olarak işaretleme)
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
      // Bildirimler silinemez
      allow delete: if false;
    }
    
    // Connection Requests koleksiyonu - Bağlantı istekleri
    match /connectionRequests/{requestId} {
      // Kullanıcı kendine gelen istekleri okuyabilir (toUserId kontrolü ile)
      // Kullanıcı gönderdiği istekleri okuyabilir (fromUserId kontrolü ile)
      allow read: if request.auth != null && (
        resource.data.toUserId == request.auth.uid ||
        resource.data.fromUserId == request.auth.uid
      );
      // Herkes istek oluşturabilir
      allow create: if request.auth != null && request.resource.data.fromUserId == request.auth.uid;
      // Kullanıcı kendine gelen istekleri güncelleyebilir (kabul/reddet için)
      allow update: if request.auth != null && resource.data.toUserId == request.auth.uid;
      // Kullanıcı gönderdiği istekleri silebilir (iptal için)
      allow delete: if request.auth != null && resource.data.fromUserId == request.auth.uid;
    }
  }
}
```

## Koleksiyonlar

- ✅ `users` - Kullanıcı bilgileri
- ✅ `posts` - Post'lar (postType: "post" veya "work")
- ✅ `jobs` - İş ilanları
- ✅ `comments` - Yorumlar (post ve job'lar için)
- ✅ `notifications` - Bildirimler (COMMENT, FOLLOW_REQUEST, INVITE)
- ✅ `connectionRequests` - Bağlantı istekleri (pending, accepted, rejected)
- ❌ `projects` - **Kaldırıldı** (artık kullanılmıyor)

## Güvenlik Açıklaması

- **read**: Tüm giriş yapan kullanıcılar okuyabilir
- **create**: Kullanıcı sadece kendi dokümanını oluşturabilir (`userId` kontrolü ile)
- **update**: 
  - Post sahibi tüm field'ları güncelleyebilir
  - Herkes sadece `likes` field'ını güncelleyebilir (beğeni için, diğer field'lar değiştirilemez)
- **delete**: Kullanıcı sadece kendi dokümanını silebilir
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

