# Drawable Dosyaları - Figma'dan Eklenmesi Gerekenler

## Zorunlu Dosyalar

### 1. Logo
- **Dosya Adı:** `ic_logo_7hive.png` veya `ic_logo_7hive.xml` (vector)
- **Kullanım Yeri:** HomeFeed, Connections, CreatePost, Jobs, Profile ekranlarının header'ında
- **Özellikler:** 
  - PNG ise: `hdpi`, `mdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi` klasörlerine farklı boyutlarda ekle
  - Vector XML ise: sadece `drawable` klasörüne tek dosya yeterli
  - Arka plan şeffaf olmalı (transparent)

### 2. Hexagon + Icon (Create Post için)
- **Dosya Adı:** `ic_add_hexagon.xml` veya `ic_add_hexagon.png`
- **Kullanım Yeri:** Bottom Navigation'da Create Post butonu
- **Özellikler:** 
  - Hexagon şekli içinde + icon
  - Vector XML tercih edilir (farklı boyutlarda iyi görünür)
  - Arka plan şeffaf

## Opsiyonel Dosyalar (İleride gerekebilir)

### 3. Empty State İkonları
- `ic_empty_posts.xml` - Post yokken gösterilecek
- `ic_empty_connections.xml` - Bağlantı yokken gösterilecek
- `ic_empty_jobs.xml` - İş yokken gösterilecek

### 4. Placeholder Profil Resmi
- `ic_profile_placeholder.xml` - Profil resmi yokken gösterilecek

---

## Dosya Formatı Önerileri

1. **Vector Drawable (XML)** - ÖNERİLEN
   - Dosya uzantısı: `.xml`
   - Farklı ekran boyutlarında otomatik ölçeklenir
   - Küçük dosya boyutu
   - Figma'dan export ederken SVG olarak export edip, SVG'yi Vector Drawable'a çevirebilirsiniz

2. **PNG** - Alternatif
   - 5 farklı density için eklemeniz gerekir:
     - `drawable-mdpi/` (48x48 dp)
     - `drawable-hdpi/` (72x72 dp)
     - `drawable-xhdpi/` (96x96 dp)
     - `drawable-xxhdpi/` (144x144 dp)
     - `drawable-xxxhdpi/` (192x192 dp)

---

## Figma'dan Export İpuçları

1. **Export Settings:**
   - Format: SVG (vektör için) veya PNG (raster için)
   - Export 1x, 2x, 3x, 4x boyutlarında (PNG için)
   - Transparent background seçin

2. **SVG to Vector Drawable:**
   - Online araç kullanabilirsiniz: https://inloop.github.io/svg2android/
   - Veya Android Studio'da: File > New > Vector Asset

---

## Icon Renklendirme Hakkında

### Bottom Navigation Icon'ları İçin:
**ÖNEMLİ:** Material Icons kullandığımız için ayrı sarı/beyaz versiyonlara gerek YOK!

Icon'ların rengini kod ile ayarlıyoruz:
- Aktif ekran: **Sarı** (#EFAF20 - BrandYellow)
- Pasif ekranlar: **Beyaz** (#FFFFFF)

Örnek kod (RootScaffold.kt):
```kotlin
Icon(
    imageVector = icon,
    tint = if (isSelected) BrandYellow else Color.White
)
```

### Özel Icon'lar İçin:
Eğer özel icon'lar ekliyorsanız (hexagon + add gibi), **tek versiyon yeterli**:
- Drawable'ı şeffaf/outline olarak ekleyin
- Renk kod ile ayarlanır: `tint` parametresi ile

**Sarı ve beyaz versiyonlarını AYRI DOSYALAR olarak eklemeyin!**

---

## Dosya Ekleme Adımları

1. Figma'dan ikonu export edin
2. Android Studio'da `app/src/main/res/drawable/` klasörüne sağ tıklayın
3. New > Vector Asset veya New > Image Asset seçin
4. Dosyayı import edin
5. Dosya adını yukarıdaki isimlerle aynı yapın
6. **Renkli versiyonları eklemeyin - kod ile renklendireceğiz!**

