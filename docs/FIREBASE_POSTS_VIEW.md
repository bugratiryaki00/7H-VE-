# Firebase'de Postları Görüntüleme

## Post Paylaşıldığında Ne Olur? ✅

1. **Post Firestore'a kaydedilir**
   - Koleksiyon: `posts`
   - Otomatik doküman ID oluşturulur
   - Alanlar: `id`, `userId`, `text`, `imageUrl`, `timestamp`

2. **Home Feed otomatik güncellenir**
   - Yeni post feed'de görünür
   - Kullanıcının bağlantılarının postları görünür

---

## Firebase Console'dan Postları Görüntüleme

### Adım 1: Firebase Console'a Giriş
1. [Firebase Console](https://console.firebase.google.com/) → Projenizi seçin

### Adım 2: Firestore Database'e Git
1. Sol menüden **Firestore Database** seçin
2. **Data** sekmesine tıklayın

### Adım 3: Posts Koleksiyonunu Bul
1. Sol panelde koleksiyonlar listelenir
2. **`posts`** koleksiyonuna tıklayın
3. Tüm postlar görünür

### Post Doküman Yapısı
Her post dokümanı şu alanları içerir:

```
posts/{postId}
  ├── id: "abc123..." (string) - Doküman ID'si ile aynı
  ├── userId: "user_uid_xyz..." (string) - Post sahibinin UID'si
  ├── text: "Post metni burada..." (string)
  ├── imageUrl: null veya "https://..." (string, opsiyonel)
  └── timestamp: 1735689600000 (number) - Unix timestamp (milisaniye)
```

---

## Post Detaylarını Görüntüleme

### Bir Post Dokümanına Tıklayın:
1. `posts` koleksiyonunda bir dokümana tıklayın
2. Tüm alanlar görünür:
   - **id**: Doküman ID'si
   - **userId**: Hangi kullanıcının paylaştığı
   - **text**: Post içeriği
   - **imageUrl**: Görsel varsa URL
   - **timestamp**: Ne zaman paylaşıldı

### Timestamp'i Okuma:
- Timestamp sayısını [Unix Timestamp Converter](https://www.unixtimestamp.com/) ile tarihe çevirebilirsiniz
- Örnek: `1735689600000` → 2024-12-31 gibi

---

## Kullanıcı Bilgisiyle Eşleştirme

### Hangi Kullanıcının Postu Olduğunu Bulma:

1. Post dokümanında `userId` alanını kopyalayın
2. Sol panelde **`users`** koleksiyonuna gidin
3. `userId` ile aynı ID'ye sahip kullanıcı dokümanını bulun
4. O kullanıcının bilgilerini görün (name, email, vb.)

**Alternatif:**
- Firebase Console → Authentication → Users
- UID'ye göre kullanıcıyı bulun

---

## Post İstatistikleri

### Kaç Post Var?
1. `posts` koleksiyonuna gidin
2. Sol üstte doküman sayısı görünür (örn: "50 documents")

### Filtreleme:
- Firebase Console'da basit filtreleme yapabilirsiniz
- Ancak gelişmiş sorgular için uygulama içinden sorgulama yapılmalı

---

## Sorun Giderme

### Post Görünmüyor?

1. **Firestore Database başlatılmış mı?**
   - Firebase Console → Firestore Database
   - Eğer "Create database" görüyorsanız → Başlatın

2. **Security Rules kontrol edin**
   - Rules → `posts` koleksiyonu için read izni var mı?
   ```javascript
   match /posts/{postId} {
     allow read: if request.auth != null;
   }
   ```

3. **Koleksiyon adı doğru mu?**
   - Koleksiyon adı: `posts` (küçük harf, çoğul)
   - Yanlış: `post`, `Posts`, `POSTS`

4. **Refresh butonuna tıklayın**
   - Firebase Console'da sayfayı yenileyin (F5)

### Post Yeni Görünmüyor?

- Biraz bekleyin (1-2 saniye)
- Firebase Console'da refresh yapın (F5)
- Post gerçekten kaydedildi mi kontrol edin (Logcat'te "Post başarıyla oluşturuldu" mesajı var mı?)

---

## Test Postu Oluşturma (Manuel)

Firebase Console'dan manuel post oluşturmak isterseniz:

1. Firebase Console → Firestore Database → Data
2. `posts` koleksiyonuna tıklayın
3. **Add document** butonuna tıklayın
4. **Auto-ID** seçin (veya manuel ID girin)
5. Alanları ekleyin:
   ```
   Field name: id
   Type: string
   Value: (doküman ID'si ile aynı olacak - otomatik ID kullanıyorsanız, önce dokümanı oluşturup sonra ekleyin)
   
   Field name: userId
   Type: string
   Value: (Firebase Auth'dan bir kullanıcının UID'sini kopyalayın)
   
   Field name: text
   Type: string
   Value: "Test post mesajı"
   
   Field name: timestamp
   Type: number
   Value: 1735689600000 (veya şu anki zaman)
   
   Field name: imageUrl
   Type: string
   Value: (boş bırakabilirsiniz veya null)
   ```

---

## Home Feed'de Görünme

Postlar Home Feed'de şu kriterlere göre görünür:
- Kullanıcının bağlantıları (connections) listesindeki kullanıcıların postları
- Kendi postları
- Timestamp'e göre sıralanır (en yeni üstte)

**Not:** Home Feed, uygulama içinden `getPostsByUserIds()` metoduyla çekilir.

