# ADR 0001: Uygulama Mimarisi Seçimi

## Durum
Önerildi

## Bağlam
Android için modern, test edilebilir ve sürdürülebilir bir mimari gereklidir. Jetpack Compose ile uyumlu, katmanlı bir yapı tercih edilir.

## Karar
MVVM mimarisi kullanılacaktır. Durum yönetimi ViewModel ile sağlanacak, veri erişimi Repository ve DataSource katmanlarına ayrılacak. DI için Hilt önerilmektedir.

## Gerekçeler
- UI ve iş mantığının ayrımı
- Test edilebilirlik ve modülerlik
- Android resmi mimari rehberleriyle uyum

## Sonuçlar
- Başlangıçta ek kurulum maliyeti
- Ekip için daha yüksek okunabilirlik ve bakımı kolaylaştırma


