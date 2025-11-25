# 7hive Software Design Document

---

## 1. Team & Roles

| Rol | İsim |
| --- | --- |
| Yazılım Takım Lideri | Ali Buğra Tiryaki |
| Yazılım Geliştirici | Mehmet Mete Öztürk |
| Yazılım Geliştirici | Umut Korkmaz |
| VCD Tasarım | Kuzey Ahlatcı |
| VCD Tasarım | Gökçe |
| VCD Tasarım | Mert Dayanıklı |

**Notlar**
- Yazılım ve tasarım ekipleri haftalık senkron toplantılar yapar.
- Tüm kritik kararlar ortak board üzerinde takip edilir (GitHub Projects / Trello).

---

## 2. Project Charter

| Başlık | Açıklama |
| --- | --- |
| **Proje Adı** | 7hive |
| **Çekirdek Problem** | Yeditepe Üniversitesi’ndeki öğrenciler, akademisyenler ve girişimciler projeleri için ekip arkadaşı, mentor ve fırsat bulmakta zorlanıyor; topluluklar izole kalıyor. |
| **Hedef Kitle** | Yeditepe Üniversitesi lisans/lisansüstü öğrencileri, akademisyen/mentorlar, girişimci topluluk ve mezun ağı. |
| **Değer Önerisi** | Doğrulanmış kampüs ağı içinde portföy paylaşımı, proje/rol açma ve akıllı eşleştirme ile gerçek iş birlikleri oluşturmayı kolaylaştırır. |
| **Başarı Kriterleri** | - MVP: email doğrulama, profil oluşturma, proje açma, eşleştirme listesi, duyuru akışı. <br> - 1. ay: ≥20 pilot kullanıcı, ≥10 proje, ≥30 öneri. <br> - Performans: ilk açılış <2 sn, listelerde akıcı scroll. |

---

## 3. User Story Mapping (Özet)

**Epics**
1. Onboarding & Auth  
2. Portföy Sergileme  
3. Proje & Rol Yönetimi  
4. Eşleştirme (Matches)  
5. Duyurular  
6. Arama & Filtreleme  
7. Profil & Rozetler  
8. Bildirimler

**Temel Story’ler**
- “Bir öğrenci olarak, @yeditepe.edu.tr emailiyle kayıt olmak istiyorum ki platforma güvenle erişeyim.”
- “Bir kullanıcı olarak, portföy kartında projelerimi göstermek istiyorum ki yeteneklerim görünür olsun.”
- “Bir proje sahibi olarak, açık rol ekleyip başvuru almak istiyorum.”
- “Bir mentor olarak, öğrencilere rozet verip onaylandırmak istiyorum.”
- “Bir kullanıcı olarak, kampüs etkinlik duyurularını tek akışta görmek istiyorum.”

### MoSCoW Önceliklendirme

| Story | Özet | Öncelik |
| --- | --- | --- |
| US-ONB-01 | @yeditepe mail ile kayıt/giriş | **MUST** |
| US-POR-01 | Portföy kartı oluştur | **MUST** |
| US-PRJ-01 | Proje aç & rol ekle | **MUST** |
| US-MAT-01 | Eşleştirme listesi | **MUST** |
| US-ANN-01 | Duyuru akışı | **MUST** |
| US-SRC-01 | Temel arama | **MUST** |
| US-COM-01 | Mesaj/bağlantı isteği | **SHOULD** |
| US-BDG-01 | Rozet/mentor onayı | **SHOULD** |
| US-NOT-01 | Bildirimler | **COULD** |
| US-CHT-01 | Gerçek zamanlı sohbet | **WON’T (bu sürüm)** |

---

## 4. System Architecture & Technology

| Başlık | Karar |
| --- | --- |
| **Mimari** | MVVM + Repository |
| **Dil** | Kotlin |
| **UI Toolkit** | Jetpack Compose (Material 3) |
| **Min SDK** | 24 (Android 7.0) – kampüs cihazlarıyla uyumlu |
| **Remote** | Firebase Auth, Firestore (gelecekte Storage/Functions) |
| **Library** | Navigation-Compose, Kotlinx Serialization, Coil |

**Veri Akışı**  
```
Compose UI → ViewModel (StateFlow) → Repository → Firestore (remote) / assets (mock)
```

**Paketleme**
- `ui/featureX` : Onboarding, Portfolio, Projects, Matches, Announcements, Profile…  
- `data/` : Repository’ler, Firestore erişimleri  
- `model/` : Kotlin data class’lar  
- `themes/` : renk, tipografi

---

## 5. Feature Specifications (Özet)

### 5.1 Onboarding & Auth
- @yeditepe mail doğrulaması, email + şifre giriş/oluşturma
- AuthViewModel + AuthRepository (FirebaseAuth)
- Profil ekranından logout → Onboarding’e dönüş

### 5.2 Portföy Kartları
- Firestore `portfolios` koleksiyonundan veri
- Kart: kullanıcı adı, bio, proje listesi
- Gelecekte fotoğraf ve link alanları eklenecek

### 5.3 Projeler & Roller
- Liste + detay ekranı
- Detayda açık roller, “Başvuru gönder” (şimdilik Snackbar, ileride Firestore write)
- `projects`, `roles` koleksiyonları

### 5.4 Eşleştirme (Matches)
- `matches` koleksiyonundan kullanıcıya göre öneriler
- Sıralama: skor (0–1 arası)
- İleride gerçek algoritma/Cloud Function

### 5.5 Duyurular
- Workshop/etkinlik akışı
- `announcements` koleksiyonu

### 5.6 Profil
- Email, doğrulama durumu, default avatar
- Çıkış butonu (Auth state reset)

---

## 6. Data Management

| Koleksiyon | Alanlar |
| --- | --- |
| `users` | id, name, department, skills, badges, availability |
| `portfolios` | userId, bio, imageUrl, projects[] |
| `projects` | id, ownerId, title, description, tags |
| `roles` | projectId, title, requiredSkills, level |
| `announcements` | title, body, dateIso, tags |
| `matches` | userId, suggestedUserId, score |

**Local DB**: Şimdilik yok; gerekirse Room ile önbellek planlanacak.

**Kurallar**  
- Geliştirme: `allow read: if true`, `allow write: if request.auth != null` (kademeli sıkılaştırılacak).  
- Prod: Yazma yetkileri email doğrulaması + rol bazlı kurallarla sınırlanacak.

---

## 7. Non-Functional Requirements

| Kategori | Detay |
| --- | --- |
| Performans | Açılış <2 sn, listeler takılmadan kaymalı |
| Güvenlik | Firebase Auth; sadece doğrulanmış kullanıcı içerik ekleyebilir |
| UI/UX | Material 3 yönergeleri, okul renk paleti |
| Maintainability | MVVM & Repository pattern, GitHub üzerinden kod review |
| Test | Manuel test checklist, ileride unit test planı |

---

## 8. Çalışma Planı & Araçlar
- **Kod & Doküman**: GitHub repo (branch/pull request akışı)
- **Tasarım**: Figma (MD3 kit), handoff’lar haftalık review
- **Takip**: GitHub Projects/Trello + haftalık sync
- **İterasyon**: SDD yaşayan doküman; major değişiklikler ekip onayı ile revize edilir.

---

## 9. Sonraki Adımlar
1. Tasarım ekibinden gelen ekranlara göre Compose UI’ları güncellemek  
2. Kayıt formuna profil alanlarını (ad, foto, skills) eklemek  
3. Rol başvurularını Firestore’a yazmak ve profil ekranında göstermek  
4. Firebase Storage/Firebase Functions ile medya & eşleştirme geliştirmeleri planlamak


