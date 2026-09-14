# Imtiyaz Tour — Android App

Source code Android native (Kotlin + Jetpack Compose) yang menyambung ke backend Node.js
(`imtiyaz-node-api`) yang kamu upload. Sama seperti sebelumnya: ini **source code siap
build**, bukan file .apk jadi — environment saya tidak bisa mengompilasi APK langsung
(perlu akses ke server Google yang tidak tersedia di sini). Cara build ada di bawah,
termasuk opsi tanpa Android Studio sama sekali.

## Fitur yang dibangun (mengikuti persis endpoint di app.js kamu)

| Tab di app | Endpoint backend | Fungsi |
|---|---|---|
| **Paket** | `GET /api/paket` | Tampilkan 5 paket umrah (Slamet, Ayem Tentrem, Linuwih, Kamulyan, Plus), tap untuk buka halaman paket |
| **Status** | `GET /api/jamaah/:id` | Jamaah cek status Lunas/Belum Lunas + sisa tagihan pakai ID mereka |
| **Bukti** | `POST /api/upload-bukti` | Pilih foto bukti transfer dari galeri, upload langsung ke WP |
| **GPS/SOS** | `POST /api/update-location`, `GET /api/jamaah-live`, `POST /api/sos` | Consent checkbox → aktifkan tracking (foreground service, kirim tiap 30 detik, **hanya saat di dalam wilayah Arab Saudi**), peta live semua jamaah, tombol SOS darurat |
| **Skrining** | `POST /api/skrining` | Form skrining kesehatan 8 kategori (A–H) |

## Update — sudah dicocokkan dengan pastiumrah.com langsung

Karena kamu konfirmasi pastiumrah.com adalah sumber data aslinya, saya cek beberapa
halaman live di situ dan sesuaikan:

- ✅ **5 paket cocok persis** dengan yang di-hardcode di `app.js` (Slamet, Ayem Tentrem,
  Linuwih, Kamulyan, Plus) — dikonfirmasi dari menu "Paket Populer" di footer situs.
- ✅ **Skrining kesehatan sudah diganti dengan 29 pertanyaan ASLI** dari
  `pastiumrah.com/formulir-skrining-kesehatan-calon-jamaah-umrah/` (bukan placeholder
  lagi) — lengkap dengan kategori A–H, pilihan jawaban ganda, dan field isian sesuai form
  aslinya. Kalau admin mengubah form ini di WordPress nanti, layar
  `SkriningScreen.kt` perlu diupdate manual mengikuti.

**⚠️ Satu hal yang perlu kamu klarifikasi** — saya temukan kejanggalan yang belum saya
tebak sendiri karena bisa salah arah:

Website punya halaman terpisah **"Cek Status Dokumen"**
(`pastiumrah.com/cek-status-dokumen/`) yang mengecek **kelengkapan dokumen** memakai
**nomor HP**, bukan status pembayaran Lunas/Belum Lunas memakai **jamaah_id** seperti
yang dijelaskan di README backend kamu. Ini bisa berarti dua hal:
1. Fitur "Status" di app harusnya memang untuk kelengkapan dokumen (bukan pembayaran),
   pakai nomor HP sebagai identifier — atau
2. Ini fitur yang benar-benar berbeda dari yang dimaksud endpoint `/api/jamaah/:id`, dan
   keduanya perlu ada di app.

Saya belum ubah apa pun di layar Status sampai kamu konfirmasi mana yang benar — supaya
saya tidak menebak dan malah menjauh dari kebutuhan aslinya.

Selain itu, saya juga lihat halaman detail tiap paket (mis. `trip-types/paket-slamet/`)
menampilkan **beberapa jadwal keberangkatan dengan harga masing-masing** (bukan cuma satu
harga "mulai dari" per paket). Kalau app perlu menampilkan jadwal per keberangkatan
(bukan cuma tipe paketnya), beri tahu saya — saat ini app hanya menampilkan level tipe
paket sesuai struktur `PAKET_EXISTING` di `app.js`.

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
