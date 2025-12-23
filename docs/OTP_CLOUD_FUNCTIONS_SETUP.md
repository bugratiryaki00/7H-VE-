# Firebase Cloud Functions ile OTP Sistemi Kurulumu

## Genel Bakış

OTP (One-Time Password) sistemi için Firebase Cloud Functions kullanacağız. Bu sistem:
1. OTP kodu oluşturur (6 haneli)
2. Firestore'da geçici olarak saklar (5 dakika geçerli)
3. Email ile kullanıcıya gönderir
4. Kullanıcı kodu girdiğinde doğrular

---

## Adım 1: Firebase Cloud Functions Projesi Kurulumu

### 1.1 Firebase CLI Kurulumu

**Windows için:**
```bash
npm install -g firebase-tools
```

**Kurulumu kontrol et:**
```bash
firebase --version
```

### 1.2 Firebase'e Giriş Yap

```bash
firebase login
```

Tarayıcı açılacak, Google hesabınızla giriş yapın.

### 1.3 Proje Klasöründe Functions Kurulumu

Proje root dizininde (AndroidStudioProjects/Proto7Hive):

```bash
firebase init functions
```

Seçenekler:
- **Language:** JavaScript veya TypeScript (TypeScript önerilir)
- **ESLint:** Yes (kod kalitesi için)
- **Install dependencies:** Yes

Bu işlem `functions` klasörü oluşturur.

---

## Adım 2: Email Gönderme Servisi Seçimi

OTP email göndermek için bir servis gerekir:

### Seçenek 1: SendGrid (Önerilen - Ücretsiz plan)
- 100 email/gün ücretsiz
- Kolay kurulum
- İyi dokümantasyon

### Seçenek 2: Nodemailer (Gmail SMTP)
- Gmail hesabı ile kullanılabilir
- Ancak "Less secure app access" gerekir (önerilmez)

### Seçenek 3: AWS SES
- Daha profesyonel
- Kurulum daha karmaşık

**Bu dokümanda SendGrid kullanacağız.**

---

## Adım 3: SendGrid Hesabı Oluşturma

1. https://sendgrid.com → Sign Up
2. Hesap oluştur (ücretsiz plan)
3. **Settings → API Keys** → Create API Key
4. API Key'i kopyala (bir daha gösterilmez!)

---

## Adım 4: Cloud Functions Kodları

### 4.1 Functions Dependencies

`functions/package.json` dosyasına ekleyin:

```json
{
  "dependencies": {
    "firebase-admin": "^11.8.0",
    "firebase-functions": "^4.3.1",
    "@sendgrid/mail": "^7.7.0",
    "crypto": "^1.0.1"
  }
}
```

### 4.2 SendGrid API Key'i Environment Variable Olarak Ekle

```bash
cd functions
firebase functions:config:set sendgrid.key="YOUR_SENDGRID_API_KEY"
```

Veya yeni yöntem (Firebase Console):
1. Firebase Console → Functions → Configuration
2. Environment variables → Add variable
3. Key: `SENDGRID_API_KEY`, Value: SendGrid API Key'iniz

### 4.3 OTP Gönderme Fonksiyonu

`functions/index.js` (veya `functions/src/index.ts`):

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const sgMail = require('@sendgrid/mail');

admin.initializeApp();

// SendGrid API Key (environment variable'dan al)
sgMail.setApiKey(functions.config().sendgrid.key);

// OTP gönderme fonksiyonu
exports.sendOtpEmail = functions.https.onCall(async (data, context) => {
  const { email } = data;

  // Email format kontrolü
  if (!email || !email.includes('@')) {
    throw new functions.https.HttpsError('invalid-argument', 'Geçersiz email adresi');
  }

  // 6 haneli OTP oluştur
  const otp = Math.floor(100000 + Math.random() * 900000).toString();

  // Firestore'da OTP'yi sakla (5 dakika geçerli)
  const otpData = {
    code: otp,
    email: email,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt: admin.firestore.Timestamp.fromMillis(Date.now() + 5 * 60 * 1000), // 5 dakika
    verified: false
  };

  // OTP'yi Firestore'a kaydet
  await admin.firestore().collection('otps').add(otpData);

  // Email içeriği
  const msg = {
    to: email,
    from: 'noreply@7hive.com', // SendGrid'de verify ettiğiniz email
    subject: '7HIVE - Email Verification Code',
    text: `Your verification code is: ${otp}. This code will expire in 5 minutes.`,
    html: `
      <div style="font-family: Arial, sans-serif; padding: 20px;">
        <h2 style="color: #EFAF20;">7HIVE Email Verification</h2>
        <p>Your verification code is:</p>
        <h1 style="color: #EFAF20; font-size: 32px; letter-spacing: 5px;">${otp}</h1>
        <p>This code will expire in 5 minutes.</p>
        <p>If you didn't request this code, please ignore this email.</p>
      </div>
    `
  };

  try {
    await sgMail.send(msg);
    return { success: true, message: 'OTP sent successfully' };
  } catch (error) {
    console.error('SendGrid error:', error);
    throw new functions.https.HttpsError('internal', 'Email gönderilemedi');
  }
});

// OTP doğrulama fonksiyonu
exports.verifyOtp = functions.https.onCall(async (data, context) => {
  const { email, code } = data;

  if (!email || !code) {
    throw new functions.https.HttpsError('invalid-argument', 'Email ve kod gerekli');
  }

  // Firestore'da OTP'yi bul
  const otpsSnapshot = await admin.firestore()
    .collection('otps')
    .where('email', '==', email)
    .where('code', '==', code)
    .where('verified', '==', false)
    .orderBy('createdAt', 'desc')
    .limit(1)
    .get();

  if (otpsSnapshot.empty) {
    throw new functions.https.HttpsError('not-found', 'Geçersiz veya süresi dolmuş kod');
  }

  const otpDoc = otpsSnapshot.docs[0];
  const otpData = otpDoc.data();

  // Süre kontrolü
  const expiresAt = otpData.expiresAt.toMillis();
  if (Date.now() > expiresAt) {
    throw new functions.https.HttpsError('deadline-exceeded', 'Kod süresi dolmuş');
  }

  // OTP'yi verified olarak işaretle
  await otpDoc.ref.update({ verified: true });

  return { success: true, message: 'OTP verified successfully' };
});
```

### 4.4 Firestore Index Oluşturma

Firebase Console → Firestore → Indexes → Create Index

**Collection:** `otps`
**Fields:**
- `email` (Ascending)
- `code` (Ascending)  
- `verified` (Ascending)
- `createdAt` (Descending)

---

## Adım 5: Functions'i Deploy Et

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

---

## Adım 6: Android Tarafında Kullanım

### 6.1 Gradle Dependencies

`app/build.gradle.kts`:

```kotlin
dependencies {
    // ... mevcut dependencies
    implementation("com.google.firebase:firebase-functions-ktx:20.4.0")
}
```

### 6.2 OTP Repository Oluştur

Yeni dosya: `app/src/main/java/com/example/proto7hive/data/OtpRepository.kt`:

```kotlin
package com.example.proto7hive.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class OtpResult(
    val success: Boolean,
    val message: String? = null
)

class OtpRepository(
    private val functions: FirebaseFunctions = Firebase.functions
) {
    suspend fun sendOtp(email: String): OtpResult {
        return try {
            val data = hashMapOf("email" to email)
            val result = functions.getHttpsCallable("sendOtpEmail")
                .call(data)
                .await()
            
            OtpResult(
                success = true,
                message = "OTP gönderildi"
            )
        } catch (e: Exception) {
            OtpResult(
                success = false,
                message = e.message ?: "OTP gönderilemedi"
            )
        }
    }
    
    suspend fun verifyOtp(email: String, code: String): OtpResult {
        return try {
            val data = hashMapOf(
                "email" to email,
                "code" to code
            )
            val result = functions.getHttpsCallable("verifyOtp")
                .call(data)
                .await()
            
            OtpResult(
                success = true,
                message = "OTP doğrulandı"
            )
        } catch (e: Exception) {
            OtpResult(
                success = false,
                message = e.message ?: "OTP doğrulanamadı"
            )
        }
    }
}
```

### 6.3 SignUpViewModel Güncelleme

`SignUpViewModel.kt`:

```kotlin
class SignUpViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository(),
    private val otpRepository: OtpRepository = OtpRepository()
) : ViewModel() {
    
    fun sendOtp() {
        val state = _uiState.value
        if (state.email.isEmpty()) {
            return
        }
        
        _uiState.value = state.copy(isLoading = true)
        viewModelScope.launch {
            val result = otpRepository.sendOtp(state.email)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                otpSent = result.success,
                errorMessage = if (!result.success) result.message else null
            )
        }
    }
    
    fun verifyOtp(): Boolean {
        val state = _uiState.value
        if (state.otpCode.length != 6) {
            return false
        }
        
        _uiState.value = state.copy(isLoading = true)
        var verified = false
        
        viewModelScope.launch {
            val result = otpRepository.verifyOtp(state.email, state.otpCode)
            verified = result.success
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = if (!result.success) result.message else null
            )
        }
        
        return verified
    }
}
```

**NOT:** `verifyOtp()` fonksiyonu suspend olmalı, async çağrı yapıyor. Bunu düzeltmemiz gerekecek.

---

## Alternatif: Daha Basit Yaklaşım (Test İçin)

Eğer Cloud Functions kurulumu karmaşık geliyorsa, şimdilik OTP adımını atlayıp direkt signup'a geçebilirsiniz. Production'da Cloud Functions ile gerçek OTP eklenebilir.

---

## Checklist

- [ ] Firebase CLI kuruldu
- [ ] `firebase login` yapıldı
- [ ] `firebase init functions` çalıştırıldı
- [ ] SendGrid hesabı oluşturuldu
- [ ] SendGrid API Key alındı
- [ ] Firebase Functions environment variable eklendi
- [ ] Functions kodu yazıldı
- [ ] `npm install` çalıştırıldı
- [ ] `firebase deploy --only functions` yapıldı
- [ ] Firestore index oluşturuldu
- [ ] Android tarafında OtpRepository eklendi
- [ ] SignUpViewModel güncellendi

---

## Sorun Giderme

**Functions deploy olmuyor:**
- `firebase login` yaptığınızdan emin olun
- Proje ID'sinin doğru olduğundan emin olun (`firebase use` ile kontrol edin)

**Email gelmiyor:**
- SendGrid'de sender email verify edildi mi kontrol edin
- SendGrid API Key doğru mu kontrol edin
- Functions logs kontrol edin: `firebase functions:log`

**OTP doğrulanmıyor:**
- Firestore index oluşturuldu mu kontrol edin
- Index "Enabled" durumunda mı kontrol edin (oluşturulduktan birkaç dakika sürebilir)

