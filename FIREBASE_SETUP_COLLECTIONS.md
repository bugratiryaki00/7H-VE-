# Firebase'de Yapılması Gerekenler - Collections Özelliği

## 1. Firestore Security Rules Güncellemesi

Firebase Console > Firestore Database > Rules sekmesine gidin ve `firestore_rules.txt` dosyasındaki güncel kuralları yapıştırın.

`collections` koleksiyonu için kurallar eklendi:
- Okuma: Giriş yapmış herkes koleksiyonları okuyabilir
- Oluşturma: Kullanıcılar sadece kendi koleksiyonlarını oluşturabilir
- Güncelleme: Sadece koleksiyon sahibi güncelleyebilir
- Silme: Sadece koleksiyon sahibi silebilir

## 2. Composite Index (Opsiyonel - Gerekirse Firebase otomatik ekler)

Eğer `getCollectionsByUserId` query'sinde `orderBy("createdAt")` kullanıyorsanız, Firebase Console'da bir index oluşturulması gerekebilir. Firebase otomatik olarak index oluşturma linkini gösterecektir (error mesajında).

Manuel olarak eklemek için:
- Firebase Console > Firestore Database > Indexes
- Collection ID: `collections`
- Fields to index:
  - `userId` (Ascending)
  - `createdAt` (Descending)

## 3. Jobs Collection Güncellemesi (Opsiyonel)

Mevcut `jobs` koleksiyonundaki dokümanlara `collectionId` alanı eklenecek. Yeni işler otomatik olarak `collectionId` ile oluşturulacak, ancak eski işler için:

- Firebase Console > Firestore Database > Data > `jobs` koleksiyonu
- Her dokümanı açın ve `collectionId` alanını ekleyin (opsiyonel, string veya null)

Not: Bu manuel bir işlemdir. Toplu güncelleme için Cloud Functions kullanılabilir (opsiyonel).

## 4. Test

1. Yeni bir koleksiyon oluşturun (CreatePost ekranında "Yeni Koleksiyon Oluştur" butonu - henüz modal eklenmediyse, Firestore Console'dan manuel ekleyin)
2. Bir iş paylaşın ve koleksiyon seçin
3. Profil sayfasındaki Works sekmesinde koleksiyonları görüntüleyin

## Önemli Notlar

- `collections` koleksiyonu ilk doküman oluşturulduğunda otomatik olarak oluşturulur
- `jobCount` alanı şimdilik manuel güncellenebilir (gelecekte Cloud Function ile otomatikleştirilebilir)
- Koleksiyon thumbnail'leri için Firebase Storage'da `collection-thumbnails/` klasörü kullanılabilir (şimdilik opsiyonel)
