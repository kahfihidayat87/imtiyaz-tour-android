# Imtiyaz Tour — Android App

Source code Android native (Kotlin + Jetpack Compose) yang menyambung ke backend Node.js
(`imtiyaz-node-api`) yang kamu upload. Sama seperti sebelumnya: ini **source code siap
build**, bukan file .apk jadi — environment saya tidak bisa mengompilasi APK langsung
(perlu akses ke server Google yang tidak tersedia di sini). Cara build ada di bawah,
termasuk opsi tanpa Android Studio sama sekali.

## Fitur yang dibangun

| Tab | Fungsi | Perlu login? |
|---|---|---|
| **Beranda** | Sapaan, jadwal shalat Makkah (Ummul Qura), daftar paket + jadwal live, tombol chat/daftar via WhatsApp | Tidak |
| **Doa & Manasik** | Kumpulan doa (teks + audio) dan panduan manasik | Tidak |
| **Layanan** | Skrining Kesehatan (native) + Evaluasi Pelayanan (buka browser) | Tidak |
| **Akun** | Login, lalu hub ke Status Pembayaran, Upload Bukti, GPS/SOS, Kelengkapan Dokumen, push notification | Ya |

## ⚠️ WAJIB — Setup Firebase sebelum build (untuk push notification)

Build APK akan **GAGAL** kalau langkah ini belum dilakukan, karena plugin
`google-services` di Gradle mencari file yang belum ada.

1. Buka [Firebase Console](https://console.firebase.google.com), buat project baru (gratis).
2. Di dalam project itu, klik **Add app → Android**. Isi package name persis:
   `com.imtiyaztour.app`
3. Download file **`google-services.json`** yang ditawarkan, lalu **taruh di folder
   `app/`** (sejajar dengan `build.gradle.kts` di dalam folder app), sebelum push ke GitHub.
4. Di Firebase Console: **Project Settings (ikon gerigi) → Service Accounts → Generate
   new private key**. Ini download file JSON kredensial SERVER (beda dari
   `google-services.json` di langkah 3).
5. Buka WP Admin → Imtiyaz Jamaah → **Pengaturan Notifikasi** (perlu `imtiyaz-connector`
   versi terbaru sudah aktif), paste seluruh isi file dari langkah 4 ke situ, simpan.
6. Setelah jamaah login sekali di app (token FCM otomatis terdaftar), test kirim
   notifikasi dari halaman Pengaturan Notifikasi tadi.

**Kalau belum mau setup Firebase dulu** dan cuma mau build APK untuk fitur lain, hapus
3 baris ini dari `app/build.gradle.kts` dan baris terkait di `build.gradle.kts` (root)
supaya build tidak gagal:
```kotlin
id("com.google.gms.google-services")   // ada di 2 file: root & app
implementation(platform("com.google.firebase:firebase-bom:..."))
implementation("com.google.firebase:firebase-messaging-ktx")
```

## ⚠️ Soal audio doa

`DoaManasikScreen.kt` berisi teks 7 doa umrah lengkap (Arab, Latin, terjemahan) dan
pemutarnya SUDAH BERFUNGSI PENUH — tapi URL audio-nya masih pola nama file
(`.../doa/01-niat-ihram.mp3` dst), **bukan file sungguhan**, karena saya tidak bisa
menyediakan/memverifikasi rekaman doa yang sah. Upload rekaman asli (idealnya dibacakan
ustadz/pembimbing manasik kalian sendiri) ke Media Library WordPress, lalu ganti nilai
`audioUrl` di file itu dengan URL aslinya.

## Base URL API

`ApiService.kt` sudah saya set ke `https://api.pastiumrah.com/` sesuai README backend
kamu — pastikan itu memang alamat final setelah deploy ke Hostinger, kalau beda tinggal
ganti satu baris `BASE_URL`.

## Cara build APK

**Opsi A — Android Studio**
1. Buka folder `imtiyaz-tour-android` sebagai project di Android Studio.
2. Tunggu Gradle sync.
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. APK ada di `app/build/outputs/apk/debug/app-debug.apk`.

**Opsi B — GitHub Actions (tanpa install apa pun)**
1. Push seluruh folder ini ke repo GitHub baru.
2. Tab **Actions** akan otomatis build. Setelah selesai, download APK dari bagian
   **Artifacts** di run tersebut.
3. Install ke HP lewat WhatsApp/Drive (aktifkan "Install dari sumber tidak dikenal").

## Batasan & catatan penting

- **Debug build**, belum signed untuk Play Store.
- **GPS tracking**: saya tambahkan pengecekan bounding-box Arab Saudi di
  `LocationForegroundService.kt` sesuai catatan di response `/` API kamu ("hanya aktif
  di Tanah Suci") — lokasi tidak dikirim ke server selama jamaah masih di Indonesia.
  Kalau area yang dimaksud lebih spesifik dari itu (mis. hanya radius kota Makkah &
  Madinah), beri tahu saya supaya bounding box-nya diperketat.
- **Peta live** pakai OpenStreetMap (osmdroid), bukan Google Maps — supaya tidak perlu
  API key berbayar. Kalau kamu sudah punya Google Maps API key dan mau tampilan yang
  familiar, bisa saya gantikan.
- **Bentuk respons `/api/jamaah/:id` dan `/api/jamaah-live`** saya asumsikan berdasarkan
  nama field yang masuk akal (`nama`, `status`, `sisa_tagihan`, `lat`, `lng`) karena
  backend memproxy ke WordPress dan saya tidak melihat contoh response asli WP-nya. Kalau
  field aslinya beda nama, kirim contoh JSON respons WP-nya dan saya sesuaikan modelnya.
- Izin GPS lengkap (`ACCESS_BACKGROUND_LOCATION`, dll) sudah didaftarkan di
  `AndroidManifest.xml` sesuai spesifikasi `gps_features.permissions` di response API
  kamu — Google Play akan meminta justifikasi tertulis untuk izin background location ini
  saat submit ke Play Store nanti.
