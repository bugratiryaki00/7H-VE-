# Users Koleksiyonu - Detaylı Açıklama

## Önemli: Her Doküman Ayrı Değerlere Sahiptir! 🎯

**Firebase Firestore'da:**
- Her **doküman** (document) bir kullanıcıyı temsil eder
- Her kullanıcının **kendi** `name`, `email`, `skills` değerleri vardır
- "Ali Buğra Tiryaki" sadece **örnek** bir değerdir
- Gerçek kullanıcılar için kendi isimlerini yazarsınız

---

## 1. String Field'lar (name, email, department, bio)

### `name` Field Örneği:

**Kullanıcı 1:**
- Document ID: `user_uid_abc123`
- `name` (string): `"Ali Buğra Tiryaki"` ✅

**Kullanıcı 2:**
- Document ID: `user_uid_xyz789`
- `name` (string): `"Mehmet Mete Öztürk"` ✅

**Kullanıcı 3:**
- Document ID: `user_uid_def456`
- `name` (string): `"Test User"` ✅

**Her kullanıcı farklı isme sahiptir!** 

Firebase Setup dokümanındaki "Ali Buğra Tiryaki" sadece **örnek** bir değerdi. Gerçek kullanıcılar için:
- Gerçek ismi yazarsınız
- Veya test için "Test User 1", "Test User 2" gibi

---

## 2. Array Field'lar (skills, interests, badges, connections)

### Array Nasıl Çalışır?

Firebase Console'da array field eklediğinizde şunu görürsünüz:
```
skills (array)
  ├─ 0: [string field - boş]
  ├─ 1: [string field - boş]
  ├─ 2: [string field - boş]
  └─ ... (istediğiniz kadar ekleyebilirsiniz)
```

**0, 1, 2... bunlar array'in index'leridir!**

### Örnek: `skills` Array'i

**Nasıl Doldurulur:**

1. `skills` field'ını ekleyin → Type: `array`
2. Firebase otomatik olarak `0` index'ini oluşturur
3. `0` alanına ilk değeri yazın: `"Kotlin"`
4. `+` butonuna tıklayarak `1` index'ini ekleyin
5. `1` alanına ikinci değeri yazın: `"Android"`
6. İstediğiniz kadar ekleyin

**Sonuç:**
```
skills (array)
  ├─ 0: "Kotlin"
  ├─ 1: "Android"
  └─ 2: "Jetpack Compose"
```

Bu, kodda `["Kotlin", "Android", "Jetpack Compose"]` array'ini oluşturur.

---

### `connections` Array'i Özel Durum

`connections` array'i diğer kullanıcıların UID'lerini tutar.

**Örnek:**

Kullanıcı A (UID: `user_abc`) şu kullanıcılarla bağlantılı:
- Kullanıcı B (UID: `user_xyz`)
- Kullanıcı C (UID: `user_def`)

**Kullanıcı A'nın dokümanı:**
```
users/user_abc
  ├─ id: "user_abc"
  ├─ name: "Ali"
  └─ connections (array)
      ├─ 0: "user_xyz"  ← Kullanıcı B'nin UID'si
      └─ 1: "user_def"  ← Kullanıcı C'nin UID'si
```

**Başlangıçta:** `connections` array'i **boş** olabilir (hiç eleman eklemeyin).

---

## Tam Örnek: Bir Kullanıcı Dokümanı

### Document ID: `test_user_uid_123`

**String Field'lar:**
```
id (string)           = "test_user_uid_123"
name (string)         = "Ali Buğra Tiryaki"
email (string)        = "test@yeditepe.edu.tr"
department (string)   = "Computer Science"
bio (string)          = "Android Developer"
profileImageUrl (string) = (boş bırakın veya URL)
```

**Number Field'lar:**
```
availability (number) = 85
```

**Array Field'lar:**
```
skills (array)
  ├─ 0: "Kotlin"
  ├─ 1: "Android"
  └─ 2: "Jetpack Compose"

interests (array)
  ├─ 0: "Mobile Development"
  └─ 1: "UI/UX"

badges (array)
  └─ (boş - hiç eleman eklemeyin)

connections (array)
  └─ (boş - başlangıçta hiç eleman eklemeyin)
```

---

## Firebase Console'da Adım Adım

### 1. String Field Eklemek

1. "+ Add field" → Field name: `name`
2. Type: `string` seçin
3. Value kısmına **kullanıcının gerçek ismini** yazın:
   - "Ali Buğra Tiryaki" (gerçek isim)
   - Veya "Test User 1" (test için)
   - Her kullanıcı için farklı!

### 2. Array Field Eklemek (skills örneği)

1. "+ Add field" → Field name: `skills`
2. Type: `array` seçin
3. Firebase otomatik olarak `0` index'ini oluşturur
4. `0` alanına ilk değeri yazın: `"Kotlin"`
5. `0` satırının yanındaki `+` butonuna tıklayın → `1` oluşur
6. `1` alanına ikinci değeri yazın: `"Android"`
7. İstediğiniz kadar ekleyin

**Array boşsa:** Hiç eleman eklemeyin, Firebase boş array olarak saklar.

### 3. Array Boş Bırakmak (connections, badges)

Bazı array'ler başlangıçta boş olabilir:
- `badges`: Kullanıcı henüz rozet kazanmamış
- `connections`: Kullanıcı henüz bağlantı yapmamış

**Yapılacaklar:**
1. "+ Add field" → Field name: `connections`
2. Type: `array` seçin
3. **Hiçbir eleman eklemeyin** (0, 1, 2... eklemeyin)
4. Save yapın

Firebase bunu `[]` (boş array) olarak saklar.

---

## Gerçek Senaryo

### Senaryo: 3 Kullanıcı Oluşturma

**Kullanıcı 1:**
- Document ID: `uid_ali` (Firebase Auth UID'si)
- `name`: `"Ali Buğra Tiryaki"`
- `email`: `"ali@yeditepe.edu.tr"`
- `skills`: `["Kotlin", "Android"]`

**Kullanıcı 2:**
- Document ID: `uid_mehmet` (Firebase Auth UID'si)
- `name`: `"Mehmet Mete Öztürk"` ← **Farklı isim!**
- `email`: `"mehmet@yeditepe.edu.tr"`
- `skills`: `["Design", "Figma"]` ← **Farklı skills!**

**Kullanıcı 3:**
- Document ID: `uid_test` (Firebase Auth UID'si)
- `name`: `"Test User"` ← **Farklı isim!**
- `email`: `"test@yeditepe.edu.tr"`
- `skills`: `[]` ← **Boş array (hiç skill yok)**

---

## Özet: Ne Yapmalısınız?

### ✅ Yapılacaklar:

1. **String field'lar için:**
   - Her kullanıcı için **kendi değerini** yazın
   - "Ali Buğra Tiryaki" sadece örnekti
   - Gerçek isimleri yazın (veya test için "Test User")

2. **Array field'lar için:**
   - `0`, `1`, `2`... bunlar array elemanlarının **index'leri**
   - Her index'e bir değer yazın
   - Boş array istiyorsanız hiç eleman eklemeyin

3. **Başlangıç için:**
   - `connections`: Boş bırakın `[]`
   - `badges`: Boş bırakın `[]`
   - `skills`, `interests`: İstediğiniz kadar ekleyin

### ❌ Yapılmayacaklar:

- ❌ Tüm kullanıcılar için aynı ismi kullanmayın
- ❌ Array field'ların `0`, `1`, `2` index'lerini boş bırakmayın (değer yazın veya hiç eklemeyin)
- ❌ Boş array için `0` index'i eklemeyin (boş string yazmayın)

---

## Sorularınızın Cevapları

**S: "Her user için ayrı olmayacak mı bu isimler?"**
**C:** Evet! Her kullanıcı **kendi** ismine sahiptir. "Ali Buğra Tiryaki" sadece örnek bir değerdi.

**S: "Array'de 0, 1, 2 çıkıyor, bunları eklememem gerekli mi?"**
**C:** 
- **Eğer array dolu olacaksa:** `0`, `1`, `2`... alanlarına değer yazın
- **Eğer array boş olacaksa:** Hiç eleman eklemeyin (0, 1, 2... eklemeyin)

**Örnek:**
- `skills` dolu olacak → `0: "Kotlin"`, `1: "Android"` ekleyin
- `connections` boş olacak → Hiçbir şey eklemeyin

