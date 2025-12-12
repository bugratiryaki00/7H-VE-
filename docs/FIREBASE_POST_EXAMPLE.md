# Posts Koleksiyonu - Detaylı Açıklama

## Şu An Yaptıklarınız ✅

Gördüğüm kadarıyla doğru yoldasınız! Şu alanları eklemişsiniz:
- ✅ `id` (string) - Document ID ile aynı
- ✅ `userId` (string) - Boş bırakılabilir şimdilik
- ✅ `text` (string) - Post metni
- ✅ `imageUrl` (string) - Opsiyonel

## ❌ Eksik: `timestamp` Alanı

**Çok önemli!** `timestamp` alanını ekleyin:

1. Firebase Console'da "+ Add field" butonuna tıklayın
2. Field name: `timestamp`
3. **Type: `number` seçin (string değil!)**
4. Value: `1735689600000` (veya şu anki zaman)

---

## Her Alan İçin Değerler

### 1. `id` (string)
**Değer:** Document ID ile aynı olmalı
- Firebase Console'da Document ID'yi kopyalayın: `x9YwYp26umGRkwGcaq4e`
- `id` field'ının value kısmına aynısını yapıştırın

### 2. `userId` (string)
**Değer:** Giriş yaptığınız kullanıcının Firebase Auth UID'si

**Nasıl Bulunur?**
1. Firebase Console → Authentication → Users
2. Kullanıcıyı seçin (test@yeditepe.edu.tr gibi)
3. UID kolonundaki değeri kopyalayın (ör: `abc123xyz...`)
4. `userId` field'ına yapıştırın

**Şimdilik test için:** Boş bırakabilirsiniz, sonra doldururuz.

### 3. `text` (string)
**Değer:** Post'un içeriği

**Örnekler:**
- "Merhaba 7HIVE! İlk postumu paylaşıyorum 🎉"
- "Bugün harika bir proje üzerinde çalışıyorum"
- "Yeni bir Android uygulaması geliştiriyorum"

### 4. `imageUrl` (string)
**Değer:** Opsiyonel - Boş bırakabilirsiniz

- Görsel yoksa: **Boş bırakın** veya hiç eklemeyin
- İleride Firebase Storage'a görsel yüklenince URL buraya gelecek

### 5. `timestamp` (number) ⚠️ EKSİK!
**Type:** `number` (string değil!)

**Değer:** Unix timestamp (milisaniye cinsinden)

**Nasıl Bulunur?**

**Yöntem 1: Şu anki zaman**
- JavaScript Console'da (tarayıcıda F12): `Date.now()` yazın
- Çıkan sayıyı kopyalayın (ör: `1735689600000`)

**Yöntem 2: Manuel değer**
- Test için: `1735689600000` (2025 başlangıcı)
- Daha yeni: `1735689700000` (yaklaşık 15 dakika sonrası)
- Daha eski: `1735689500000` (yaklaşık 15 dakika öncesi)

**Örnek timestamp değerleri:**
```
1735689600000  // 2025-01-01 00:00:00 (yaklaşık)
1735689700000  // Biraz daha yeni
1735689800000  // Daha da yeni
```

**ÖNEMLİ:** Timestamp, postların sıralanması için kullanılır. Yeni postlar daha büyük timestamp'e sahip olmalı.

---

## Tamamlanmış Post Örneği

```
Document ID: x9YwYp26umGRkwGcaq4e

Fields:
├─ id (string)          = "x9YwYp26umGRkwGcaq4e"
├─ userId (string)      = "abc123xyz..." (Firebase Auth UID)
├─ text (string)        = "Merhaba 7HIVE! İlk postumu paylaşıyorum 🎉"
├─ imageUrl (string)    = (boş bırakın)
└─ timestamp (number)   = 1735689600000
```

---

## Firebase Console'da Ekleme Adımları

1. **`timestamp` alanını ekleyin:**
   - "+ Add field" → `timestamp` → Type: `number` → Value: `1735689600000`

2. **`id` değerini doldurun:**
   - Document ID'yi kopyalayın (`x9YwYp26umGRkwGcaq4e`)
   - `id` field'ına yapıştırın

3. **`userId` değerini bulun:**
   - Authentication → Users → UID'yi kopyalayın
   - `userId` field'ına yapıştırın

4. **`text` değerini yazın:**
   - Test mesajı yazın (ör: "Test post")

5. **Save** butonuna tıklayın

---

## Kontrol Listesi

- [ ] `id` field'ı Document ID ile aynı
- [ ] `userId` field'ı Firebase Auth UID ile dolu (veya boş)
- [ ] `text` field'ında bir mesaj var
- [ ] `timestamp` field'ı eklendi (type: number!)
- [ ] `imageUrl` boş bırakıldı (opsiyonel)
- [ ] Save yapıldı

---

## Notlar

1. **Type Önemli:** `timestamp` için `number` seçin, `string` değil!
2. **UID Bulma:** Authentication → Users → UID kolonu
3. **Timestamp:** Postların sıralanması için gerekli
4. **İlk Test:** `userId`'yi boş bırakıp sadece `text` ve `timestamp` ile test edebilirsiniz

