# Post Paylaşma Sorun Giderme

## Sorun: Share butonuna tıklayınca post paylaşılmıyor

### 1. Firebase Console'da Firestore Security Rules Kontrolü ✅

**Adımlar:**
1. Firebase Console → **Firestore Database** → **Rules** sekmesi
2. Şu kuralların olduğundan emin olun:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Posts koleksiyonu - Giriş yapanlar okuyabilir, kendi postlarını oluşturabilir
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // ... diğer kurallar
  }
}
```

3. **Publish** butonuna tıklayın

**Önemli:** Eğer kurallar yoksa veya yanlışsa, post oluşturma çalışmaz!

---

### 2. Android Studio Logcat ile Hata Kontrolü

**Adımlar:**
1. Android Studio → **Logcat** (alt panel)
2. Filter: `CreatePostViewModel` yazın
3. Post paylaşmayı deneyin
4. Hata mesajlarını kontrol edin

**Olası Hata Mesajları:**
- `PERMISSION_DENIED` → Security Rules sorunu
- `UNAUTHENTICATED` → Kullanıcı giriş yapmamış
- `NETWORK_ERROR` → İnternet bağlantısı sorunu

---

### 3. Firestore Database Başlatılmış mı?

**Kontrol:**
1. Firebase Console → **Firestore Database**
2. Eğer "Create database" görüyorsanız → Başlatın
3. Location seçin (örn: `us-central1`)
4. Enable'e tıklayın

---

### 4. Posts Koleksiyonu Oluşturulmuş mu?

**Kontrol:**
1. Firebase Console → **Firestore Database** → **Data** sekmesi
2. `posts` koleksiyonu var mı?
3. Yoksa, otomatik oluşturulacak (ilk post paylaşımında)

**İlk Doküman Oluşturma:**
- Uygulama çalışırken koleksiyon otomatik oluşur
- Manuel oluşturmaya gerek yok

---

### 5. Kullanıcı Giriş Yapmış mı?

**Kontrol:**
1. Uygulamada **Profile** ekranına gidin
2. Kullanıcı bilgileri görünüyor mu?
3. Görünmüyorsa → Login ekranına gidip giriş yapın

---

### 6. İnternet Bağlantısı

**Kontrol:**
- Telefon/emülatörde internet çalışıyor mu?
- Browser'da google.com'a erişebiliyor musunuz?

---

## Test Adımları

### Adım 1: Logcat'i Açın
```
Android Studio → Logcat → Filter: "CreatePostViewModel"
```

### Adım 2: Post Paylaşmayı Deneyin
1. Create Post ekranını açın
2. Bir metin yazın
3. "share" butonuna tıklayın

### Adım 3: Logcat'te Şunları Arayın
- ✅ `Post oluşturuluyor: userId=...` → İyi, post oluşturma başladı
- ✅ `Post başarıyla oluşturuldu: postId=...` → Başarılı!
- ❌ `Post oluşturma hatası` → Hata var, mesajı okuyun

### Adım 4: Firebase Console'da Kontrol
1. Firebase Console → Firestore Database → Data
2. `posts` koleksiyonuna bakın
3. Yeni post görünüyor mu?

---

## Hızlı Çözüm

1. **Security Rules'u kontrol edin ve düzeltin** (en yaygın sorun)
2. **Firestore Database'in başlatıldığından emin olun**
3. **Kullanıcının giriş yaptığından emin olun**
4. **Logcat'te hata mesajlarını kontrol edin**

---

## Hala Çalışmıyorsa

1. Logcat'teki **tam hata mesajını** kopyalayın
2. Firebase Console → Firestore Database → Rules → **Rules simulator** ile test edin
3. Android Studio → **Build → Clean Project** → **Rebuild Project**

