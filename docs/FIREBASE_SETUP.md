# Firebase Firestore Yapılandırması - 7HIVE

## Koleksiyonlar ve Alan Yapıları

### 1. `posts` Koleksiyonu

Her doküman bir post'u temsil eder.

**Koleksiyon:** `posts`

**Doküman ID:** Otomatik (Firestore tarafından oluşturulur)

**Alanlar:**
```
id: string (doküman ID'si ile aynı)
userId: string (post'u paylaşan kullanıcının Firebase Auth UID'si)
text: string (post metni)
imageUrl: string? (opsiyonel, görsel URL'si - Firebase Storage'dan gelecek)
timestamp: number (Unix timestamp - milisaniye cinsinden, örn: 1735689600000)
```

**Örnek Doküman:**
```
posts/{postId}
  id: "post123"
  userId: "user_uid_abc123"
  text: "Merhaba 7HIVE! İlk postumu paylaşıyorum 🎉"
  imageUrl: null (veya "https://firebasestorage.googleapis.com/...")
  timestamp: 1735689600000
```

---

### 2. `users` Koleksiyonu

Her doküman bir kullanıcıyı temsil eder.

**Koleksiyon:** `users`

**Doküman ID:** Firebase Auth UID'si (kullanıcının giriş yaptığı UID)

**Alanlar:**
```
id: string (doküman ID'si ile aynı, Firebase Auth UID)
name: string (kullanıcı adı)
email: string (e-posta adresi, @yeditepe.edu.tr ile bitmeli)
department: string? (opsiyonel, bölüm)
skills: array<string> (yetenekler listesi)
interests: array<string> (ilgi alanları listesi)
badges: array<string> (rozetler listesi)
availability: number? (opsiyonel, 0-100 arası uygunluk skoru)
connections: array<string> (bağlantı yapılan kullanıcıların UID'leri)
profileImageUrl: string? (opsiyonel, profil resmi URL'si)
bio: string? (opsiyonel, kısa biyografi)
```

**Örnek Doküman:**
```
users/{userId} (userId = Firebase Auth UID)
  id: "user_uid_abc123"
  name: "Ali Buğra Tiryaki"
  email: "test@yeditepe.edu.tr"
  department: "Computer Science"
  skills: ["Kotlin", "Android", "Jetpack Compose"]
  interests: ["Mobile Development", "UI/UX"]
  badges: []
  availability: 85
  connections: ["user_uid_xyz789", "user_uid_def456"]
  profileImageUrl: null
  bio: "Android Developer | 7HIVE Team Lead"
```

**Subcollection:** `users/{userId}/savedJobs`

Kaydedilen iş ilanları için:
```
users/{userId}/savedJobs/{jobId}
  savedAt: number (Unix timestamp - milisaniye)
```

---

### 3. `jobs` Koleksiyonu

İş ilanlarını temsil eder.

**Koleksiyon:** `jobs`

**Doküman ID:** Otomatik

**Alanlar:**
```
id: string (doküman ID'si ile aynı)
title: string (iş başlığı, örn: "UI/UX Designer")
company: string (şirket adı, örn: "Tech Startup")
location: string (lokasyon, örn: "Istanbul", "Remote", "Hybrid")
workType: string (çalışma tipi: "Full-time", "Part-time", "Remote", "Hybrid", "On-site")
description: string (iş açıklaması)
requiredSkills: array<string> (gerekli yetenekler)
imageUrl: string? (opsiyonel, şirket/logo görseli)
userId: string (işi paylaşan kullanıcının UID'si - işveren)
```

**Örnek Doküman:**
```
jobs/{jobId}
  id: "job123"
  title: "Motion Designer – Tech Startup"
  company: "Tech Startup"
  location: "Remote"
  workType: "Full-time"
  description: "We are looking for a creative motion designer..."
  requiredSkills: ["After Effects", "Premiere Pro", "Motion Graphics"]
  imageUrl: null
  userId: "user_uid_abc123"
```

---

### 4. `projects` Koleksiyonu (Mevcut - Değişmeden Kalıyor)

Projeleri temsil eder (WORKS için kullanılacak).

**Koleksiyon:** `projects`

**Alanlar:**
```
id: string
ownerId: string (proje sahibinin UID'si)
title: string
description: string
tags: array<string>
imageUrl: string?
```

---

## Firebase Console'da Oluşturma Adımları

### Adım 1: `posts` Koleksiyonu Oluştur

1. Firebase Console → Firestore Database
2. "Start collection" veya mevcut koleksiyonun yanındaki "+" butonuna tıkla
3. Collection ID: `posts`
4. Document ID: "Auto-ID" seç (otomatik oluşturulsun)
5. İlk dokümanı eklemek için alanları ekle:
   - Field: `userId`, Type: `string`, Value: `test_user_id`
   - Field: `text`, Type: `string`, Value: `İlk test postu`
   - Field: `timestamp`, Type: `number`, Value: `1735689600000`
   - Field: `imageUrl`, Type: `string`, Value: (boş bırak veya null)
6. Save

**Test için birkaç post ekleyin:**
- Giriş yaptığınız kullanıcının UID'sini `userId` olarak kullanın
- Farklı `timestamp` değerleri verin (sıralama için)

---

### Adım 2: `users` Koleksiyonu Oluştur

1. Collection ID: `users`
2. Document ID: **Manuel girin** - Giriş yaptığınız kullanıcının Firebase Auth UID'sini yazın
   - Firebase Console → Authentication → Users → Kullanıcıyı seç → UID'yi kopyala
3. Alanları ekle:
   - `id`: string (UID ile aynı)
   - `name`: string (ör: "Test User")
   - `email`: string (ör: "test@yeditepe.edu.tr")
   - `department`: string (opsiyonel)
   - `skills`: array (boş array `[]` veya `["Kotlin"]` gibi)
   - `interests`: array
   - `badges`: array
   - `availability`: number (opsiyonel, 0-100)
   - `connections`: array (boş başlayabilir `[]`)
   - `profileImageUrl`: string (opsiyonel, null bırakabilirsiniz)
   - `bio`: string (opsiyonel)

**Önemli:** `connections` array'ini başlangıçta boş `[]` olarak bırakın. Uygulama içinden ekleyeceğiz.

---

### Adım 3: `jobs` Koleksiyonu Oluştur

1. Collection ID: `jobs`
2. Document ID: "Auto-ID"
3. Alanları ekle:
   - `id`: string (doküman ID ile aynı)
   - `title`: string
   - `company`: string
   - `location`: string
   - `workType`: string (`Full-time`, `Part-time`, `Remote`, `Hybrid`, `On-site` gibi)
   - `description`: string
   - `requiredSkills`: array
   - `imageUrl`: string (opsiyonel)
   - `userId`: string (işi paylaşan kullanıcının UID'si)

---

## Test Verileri Eklemek İçin

### Test Post Oluşturma:

1. Firebase Console → Authentication → Users
2. Kullanıcının UID'sini kopyala (ör: `abc123xyz...`)
3. Firestore → `posts` koleksiyonu → Add document
4. Auto-ID seç
5. Alanları ekle:
   ```
   userId: "abc123xyz..." (kopyaladığın UID)
   text: "Merhaba! İlk postumu paylaşıyorum 🎉"
   timestamp: 1735689600000 (veya şu anki zamanı: Date.now() JavaScript'te)
   imageUrl: (boş bırak)
   ```

### Test User Oluşturma:

1. Firestore → `users` koleksiyonu
2. Add document → **Manuel ID girin** (Authentication'dan kopyaladığın UID)
3. Alanları ekle:
   ```
   id: "abc123xyz..." (UID ile aynı)
   name: "Test User"
   email: "test@yeditepe.edu.tr"
   department: "Computer Science"
   skills: ["Kotlin", "Android"]
   interests: ["Mobile Dev"]
   badges: []
   connections: [] (boş array - başlangıç için)
   ```

---

## Security Rules (Test İçin)

Firebase Console → Firestore Database → Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Test için: Herkes okuyabilsin, sadece giriş yapanlar yazabilsin
    match /{document=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

**ÖNEMLİ:** Bu kurallar test içindir. Production'da daha sıkı kurallar kullanılmalı!

---

## Checklist

- [ ] `posts` koleksiyonu oluşturuldu
- [ ] `users` koleksiyonu oluşturuldu (UID ile manuel doküman)
- [ ] `jobs` koleksiyonu oluşturuldu
- [ ] `users/{userId}` dokümanında `connections` array'i var (boş olabilir)
- [ ] En az 1 test post eklendi (`userId` doğru UID ile)
- [ ] Security rules güncellendi (test için)

---

## Notlar

1. **UID Nasıl Bulunur?**
   - Firebase Console → Authentication → Users
   - Kullanıcıyı seç → UID kolonunda görünür

2. **Timestamp Değeri:**
   - JavaScript: `Date.now()` veya `new Date().getTime()`
   - Şu anki zaman için: ~1735689600000 (2025 başlangıcı)
   - Test için farklı zamanlar verebilirsiniz (sıralama için)

3. **Array Alanlar:**
   - Firestore Console'da array alanı eklerken, önce "Add field" → Type: "array" seç
   - Sonra içine string'ler ekleyebilirsiniz

4. **Null/Opsiyonel Alanlar:**
   - `imageUrl`, `profileImageUrl`, `bio`, `department`, `availability` gibi alanlar opsiyonel
   - Eklemeseniz de olur, kodda default değerler var

