## Software Design Document (SDD)

### 1. Giriş
1.1 Proje Özeti: [Project Charter özetine bağlayın]
1.2 Terimler ve Kısaltmalar: MVVM, DI, DAO, DTO, etc.

### 2. Mimari ve Teknoloji Yığını
2.1 Mimari Desen: MVVM (gerekçe: test edilebilirlik, ayrık katmanlar, Compose uyumu)
2.2 Teknolojiler
- Dil: Kotlin
- UI: Jetpack Compose + Material 3
- Navigasyon: Navigation-Compose
- Min SDK: 24 (Android 7.0) — kampüs cihazları için makul taban, modern API'lerle uyumlu
- Kütüphaneler: Hilt (DI), Retrofit/OkHttp (ağ - ileri aşama), Room (yerel - ileri aşama), Kotlinx Serialization (mock veri), Coil (görsel)

### 3. Yüksek Seviyeli Tasarım
3.1 Modül Ayrımı: Öğrenme amaçlı tek modül `:app` ile başlanacak. İleride `:core`, `:data`, `:feature:*` olarak bölünebilir.
3.2 Veri Akış Diyagramı: UI -> ViewModel -> Repository -> DataSource (Local/Remote)

### 4. Özellik Detayları (MVP Odaklı)
Her kullanıcı hikâyesi için aşağıdakileri doldurun:
- Özellik Adı:
- Kullanıcı Hikâyesi:
- Kabul Kriterleri:
- UI/UX Bağımlılıkları:
- Teknik Uygulama Planı:
  - Ekran(lar):
  - ViewModel(ler):
  - Veri Modelleri:
  - Veri Kaynağı:
  - İzinler:

Örnek: "Portföy Listeleme"
- Ekranlar: `PortfolioListScreen`, `PortfolioDetailScreen`
- ViewModel: `PortfolioViewModel` (UI durumu + mock repository)
- Veri Modelleri: `User`, `Project`, `PortfolioCard`
- Veri Kaynağı: Başlangıçta `assets/` içinde JSON (kotlinx serialization ile), sonrasında API
- İzinler: INTERNET (varsayılan), Bildirimler (sonraki aşama)

### 5. Veri Yönetimi
5.1 Yerel DB: Room kullanımı, varlıklar ve ilişkiler
5.2 Uzak Veri: Örnek API uç noktası ve JSON şeması
```
GET /api/v1/portfolios
Response: [
  {"id":"u1","name":"Ada","skills":["Kotlin","UI"],"projects":[...]} 
]
```

### 6. Fonksiyonel Olmayan Gereksinimler
- Performans: Açılış < 2 sn, liste kaydırma akıcı
- Kullanılabilirlik: Material 3 yönergeleri
- Sürdürülebilirlik: Tutarlı kod stili, modüler mimari

### 7. Test ve Devreye Alma
- Unit/UI test stratejisi (JUnit, Espresso/Compose UI Test)
- CI/CD (örn. GitHub Actions) [isteğe bağlı]


