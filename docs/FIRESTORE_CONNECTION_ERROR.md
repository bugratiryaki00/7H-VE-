# Firestore "Network Error" Sorunu Çözümü

## Hata Mesajı
```
network error (such as timeout, interrupted connection or unreachable host...)
```

## Olası Nedenler ve Çözümler

### 1. Firestore Database Başlatılmamış ❌

**Kontrol:**
1. Firebase Console → Firestore Database
2. "Create database" butonu görünüyor mu?

**Çözüm:**
1. Firebase Console → Firestore Database → "Create database"
2. **Production mode** veya **Test mode** seçin (Test mode daha kolay)
3. Location seçin (örn: `us-central1`)
4. Enable'e tıklayın

### 2. Emülatör İnternet Bağlantısı Yok ❌ ⚠️ EN YAYGIN SORUN

**Kontrol:**
- Emülatör'de browser açıp bir website'e erişmeyi deneyin (örn: google.com)

**Hızlı Çözümler:**
1. **Emülatörü Cold Boot ile yeniden başlat:**
   - Android Studio → AVD Manager → Emülatör yanındaki ▼ → **Cold Boot Now**

2. **Emülatör'de WiFi'yi kontrol et:**
   - Settings → Wi-Fi → WiFi'yi kapat/aç

3. **DNS ayarlarını değiştir:**
   - AVD Manager → Edit emülatör → Show Advanced Settings → DNS: `8.8.8.8,8.8.4.4`

4. **Windows Proxy ayarlarını kontrol et:**
   - Settings → Network & Internet → Proxy → Kapalı olduğundan emin ol

**Detaylı çözümler için:** `docs/EMULATOR_INTERNET_FIX.md` dosyasına bakın

### 3. google-services.json Yanlış/Eksik ❌

**Kontrol:**
- `app/google-services.json` dosyası var mı?
- Dosya içinde `project_id` doğru mu?

**Çözüm:**
1. Firebase Console → Project Settings → Your apps → Android app
2. `google-services.json` dosyasını indirin
3. `app/` klasörüne kopyalayın (eski dosyanın üzerine yazın)
4. Android Studio'da Sync Project with Gradle Files

### 4. Firestore Rules Çok Kısıtlayıcı ❌

**Kontrol:**
- Firebase Console → Firestore Database → Rules
- Mevcut kurallar ne?

**Çözüm:**
Test için geçici olarak şunu kullanın:

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

### 5. Firebase Projesi Yanlış ❌

**Kontrol:**
- `google-services.json` içindeki `project_id` Firebase Console'daki proje ID'si ile eşleşiyor mu?

**Çözüm:**
1. Firebase Console → Project Settings → General
2. Project ID'yi kontrol edin
3. `google-services.json` dosyasını kontrol edin

## Adım Adım Kontrol Listesi

- [ ] Firebase Console'da Firestore Database oluşturuldu mu?
- [ ] Firestore location seçildi mi? (örn: us-central1)
- [ ] `google-services.json` dosyası `app/` klasöründe mi?
- [ ] Android Studio'da Sync Project with Gradle Files yapıldı mı?
- [ ] Emülatör internet bağlantısı var mı?
- [ ] Firestore Security Rules publish edildi mi?
- [ ] Firebase Authentication → Email/Password enabled mi?

## Test İçin Basit Kurallar

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

## Hala Çalışmıyorsa

1. **Android Studio → Build → Clean Project**
2. **Android Studio → Build → Rebuild Project**
3. **Emülatör'ü restart edin**
4. **Firebase Console → Firestore Database → Data** - Koleksiyonlar görünüyor mu?
5. **Logcat'te detaylı hata mesajlarını kontrol edin**

