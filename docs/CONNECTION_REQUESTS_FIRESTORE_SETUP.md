# Connection Requests Firestore Setup

## Firestore Security Rules

Firestore Security Rules zaten güncellenmiş durumda (`FIRESTORE_SECURITY_RULES.md` dosyasına bakın). `connectionRequests` koleksiyonu için kurallar eklenmiştir.

## Firestore Indexes

Connection requests için **2 adet composite index** oluşturmanız gerekiyor:

### Index 1: Pending Requests (Gelen İstekler)

Bu index, kullanıcıya gelen bekleyen istekleri getirmek için kullanılır.

**Manuel Index Oluşturma:**
1. Firebase Console → Firestore Database → Indexes sekmesi
2. "Create Index" butonuna tıklayın
3. Collection ID: `connectionRequests`
4. Fields (sırayla ekleyin):
   - `toUserId`: Ascending
   - `status`: Ascending
   - `timestamp`: Descending
5. Query scope: Collection
6. "Create" butonuna tıklayın

### Index 2: Sent Requests (Gönderilen İstekler)

Bu index, kullanıcının gönderdiği bekleyen istekleri getirmek için kullanılır.

**Manuel Index Oluşturma:**
1. Firebase Console → Firestore Database → Indexes sekmesi
2. "Create Index" butonuna tıklayın
3. Collection ID: `connectionRequests`
4. Fields (sırayla ekleyin):
   - `fromUserId`: Ascending
   - `status`: Ascending
   - `timestamp`: Descending
5. Query scope: Collection
6. "Create" butonuna tıklayın

## Index Özeti

| Collection | Fields | Query Scope |
|------------|--------|-------------|
| `connectionRequests` | `toUserId` (Asc), `status` (Asc), `timestamp` (Desc) | Collection |
| `connectionRequests` | `fromUserId` (Asc), `status` (Asc), `timestamp` (Desc) | Collection |

## ConnectionRequest Model

```typescript
{
  id: string
  fromUserId: string      // İsteği gönderen kullanıcı
  toUserId: string        // İsteği alan kullanıcı
  status: string          // "pending", "accepted", "rejected"
  timestamp: number       // Unix timestamp (milisaniye)
}
```

## Status Values

- **pending**: İstek gönderildi, henüz yanıtlanmadı
- **accepted**: İstek kabul edildi, connection oluşturuldu
- **rejected**: İstek reddedildi

## Notlar

- Indexlerin oluşması birkaç dakika sürebilir
- Index oluşturma tamamlanana kadar sorgular çalışmayabilir
- Index durumunu Indexes sekmesinden kontrol edebilirsiniz (Building/Enabled)

