# Notifications Firestore Setup

**Not:** Connection Requests için indexler gerekiyorsa `CONNECTION_REQUESTS_FIRESTORE_SETUP.md` dosyasına bakın.

## Firestore Security Rules

Firebase Console → Firestore Database → Rules sekmesine güncellenmiş kuralları ekleyin:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // ... diğer kurallar ...
    
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
  }
}
```

**Önemli:** Rules'ı ekledikten sonra mutlaka **Publish** butonuna tıklayın!

## Firestore Index

Notifications koleksiyonunda `userId` ve `timestamp` alanlarına göre sıralama yapıldığı için bir composite index gerekebilir.

**Index otomatik oluşturulur:**
- İlk bildirim okuma işleminde Firebase Console'da bir link göreceksiniz
- Bu linke tıklayarak index'i otomatik oluşturabilirsiniz

**Manuel Index Oluşturma:**
1. Firebase Console → Firestore Database → Indexes sekmesi
2. "Create Index" butonuna tıklayın
3. Collection ID: `notifications`
4. Fields:
   - `userId`: Ascending
   - `timestamp`: Descending
5. Query scope: Collection
6. "Create" butonuna tıklayın

## Notification Model

```typescript
{
  id: string
  userId: string          // Bildirimi alan kullanıcı
  fromUserId: string      // Bildirimi gönderen kullanıcı
  type: string            // "COMMENT", "FOLLOW_REQUEST", "INVITE"
  relatedId: string?      // postId, jobId, projectId, vs.
  relatedType: string?    // "post", "job", "project", "team"
  message: string         // Bildirim mesajı
  timestamp: number       // Unix timestamp (milisaniye)
  isRead: boolean         // Okundu mu?
}
```

## Notification Types

- **COMMENT**: Birisi postunuza veya iş ilanınıza yorum yaptı
- **FOLLOW_REQUEST**: Birisi size connect isteği gönderdi
- **INVITE**: Birisi sizi projeye, işe veya ekibe davet etti

