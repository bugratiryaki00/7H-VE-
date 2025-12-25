# Emülatör İnternet Bağlantısı Sorunu Çözümü

## Sorun
Emülatörde internet yok, Firestore'a bağlanılamıyor.

## Çözümler

### Yöntem 1: Emülatörü Yeniden Başlat (En Kolay)

1. Android Studio → AVD Manager
2. Emülatörü **Cold Boot** ile başlat:
   - Emülatörün yanındaki ▼ → **Cold Boot Now**
   - Veya emülatörü kapat → Yeniden başlat

### Yöntem 2: WiFi Ayarlarını Kontrol Et

1. Emülatör'de **Settings** açın
2. **Wi-Fi** veya **Network** → Ağ bağlantısını kontrol edin
3. WiFi'yi kapatıp açın

### Yöntem 3: DNS Ayarlarını Değiştir

1. Android Studio → AVD Manager
2. Emülatörün yanındaki **Edit** (kalem ikonu)
3. **Show Advanced Settings**
4. **Network** → **DNS Settings**
5. **8.8.8.8,8.8.4.4** (Google DNS) yazın
6. **Finish** → Emülatörü yeniden başlatın

### Yöntem 4: Proxy Ayarlarını Kontrol Et

1. Windows → **Settings** → **Network & Internet** → **Proxy**
2. Eğer proxy ayarları varsa, bunları kapatın veya emülatör için bypass ekleyin

### Yöntem 5: Emülatör Ağ Ayarlarını Sıfırla

1. Android Studio → AVD Manager
2. Emülatörü **Wipe Data** (verileri sil)
3. Yeniden oluştur veya başlat

### Yöntem 6: Android Studio Ağ Ayarlarını Kontrol Et

1. **File** → **Settings** (Windows) / **Preferences** (Mac)
2. **Appearance & Behavior** → **System Settings** → **HTTP Proxy**
3. **No proxy** seçili olduğundan emin olun

### Yöntem 7: Fiziksel Cihaz Kullan

Emülatör sorunlu ise:
1. Fiziksel Android cihazınızı USB ile bağlayın
2. USB Debugging'i açın
3. Android Studio'da cihazı seçin

## Hızlı Test

Emülatör'de:
1. Browser açın
2. Google.com'a gidin
3. Çalışıyor mu kontrol edin

## Hala Çalışmıyorsa

1. **Android Studio → File → Invalidate Caches / Restart**
2. **Build → Clean Project**
3. **Build → Rebuild Project**
4. Emülatörü yeniden başlat



