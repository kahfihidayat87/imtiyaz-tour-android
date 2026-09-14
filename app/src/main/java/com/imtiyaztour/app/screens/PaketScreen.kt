package com.imtiyaztour.app.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*
import com.imtiyaztour.app.R
import kotlinx.coroutines.tasks.await

// Nomor WhatsApp admin resmi IMTIYAZ (0811-277-6543) dalam format internasional.
private const val WA_ADMIN_NUMBER = "628112776543"

// PENTING: layar ini TIDAK membuka WebView atau browser eksternal untuk detail paket.
// Satu-satunya intent ke aplikasi lain di sini adalah membuka WhatsApp lewat tombol
// "Daftar Paket Ini"/"Chat Admin" - permintaan eksplisit, sama kategorinya dengan
// membuka aplikasi telepon/kontak, bukan menampilkan konten web di dalam app.

@Composable
fun PaketScreen(namaJamaah: String, onLogout: () -> Unit, onOpenAkun: () -> Unit = {}) {
    val context = LocalContext.current
    var paketList by remember { mutableStateOf<List<Paket>>(emptyList()) }
    var allTrips by remember { mutableStateOf<List<TripLive>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedPaket by remember { mutableStateOf<Paket?>(null) }

    LaunchedEffect(Unit) {
        try {
            val live = ImtiyazApi.service.getPaketLive()
            paketList = live.existing_types
            allTrips = live.latest_trips
        } catch (e: Exception) {
            try {
                paketList = ImtiyazApi.service.getPaket()
            } catch (e2: Exception) {
                errorMsg = "Gagal memuat paket - periksa koneksi. (${e2.message})"
            }
        } finally {
            loading = false
        }
    }

    val current = selectedPaket
    if (current != null) {
        val tripsForThisType = allTrips.filter { it.tipe == current.tipe || it.tipe_slug == current.id }
        PaketDetailScreen(paket = current, trips = tripsForThisType, onBack = { selectedPaket = null })
        return
    }

    Column(Modifier.fillMaxSize().background(CreamBg)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { HeroHeader(namaJamaah, onOpenAkun) }
            item { AlamatKantorBar() }
            item { JadwalShalatCard() }
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PILIH PAKET UMRAH", color = TextDarkGreen, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(28.dp).height(1.dp).background(BrandGold))
                        Text(" ◈ ", color = BrandGold, fontSize = 11.sp)
                        Box(Modifier.width(28.dp).height(1.dp).background(BrandGold))
                    }
                }
            }
            when {
                loading -> item {
                    Box(Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandGreen)
                    }
                }
                errorMsg != null -> item { Text(errorMsg!!, color = Danger, fontSize = 13.sp) }
                else -> items(paketList) { paket ->
                    val jumlahJadwal = allTrips.count { it.tipe == paket.tipe || it.tipe_slug == paket.id }
                    PaketCard(paket, jumlahJadwal) { selectedPaket = paket }
                }
            }
            item {
                Spacer(Modifier.height(2.dp))
                WhatsappCta("💬 Chat Admin WhatsApp") {
                    bukaWhatsappAdmin(context, "Assalamu'alaikum, saya ingin bertanya tentang paket umrah IMTIYAZ.")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HeroHeader(namaJamaah: String, onOpenAkun: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BrandGreen, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("☰", color = BrandGoldSoft, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔔", fontSize = 16.sp, modifier = Modifier.padding(end = 14.dp))
                Box(Modifier.clickable(onClick = onOpenAkun)) {
                    Text("👤", fontSize = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(56.dp).background(Color(0xFF04241C), CircleShape).border(2.dp, BrandGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_imtiyaz),
                    contentDescription = "Logo Imtiyaz Tour",
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Selamat datang di", color = Sand.copy(alpha = 0.8f), fontSize = 12.sp)
                Text(
                    "Imtiyaz Tour", color = BrandGoldSoft, fontSize = 27.sp,
                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif
                )
                if (namaJamaah.isNotBlank()) {
                    Text(namaJamaah, color = Sand, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 1.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Biro Resmi Penyelenggara Ibadah Umrah (PPIU) No. 383/2021 - Travel Umrah Nyaman Lansia #1 Terbaik",
            color = Sand.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

@Composable
private fun AlamatKantorBar() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(14.dp))
            .border(1.dp, BrandGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("📍", fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            "Jln. Pertapan, Tegal Cerme Rt. 08, Baturetno, Banguntapan, Bantul, Yogyakarta — 50m selatan kantor Kec. Banguntapan",
            color = TextDarkGreen, fontSize = 11.5.sp, lineHeight = 16.sp
        )
    }
}

@Composable
private fun WhatsappCta(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(54.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).background(WhatsappGreen, CircleShape), contentAlignment = Alignment.Center) {
                Text("📞", fontSize = 11.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(label.removePrefix("💬 "), color = TextDarkGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

private fun bukaWhatsappAdmin(context: android.content.Context, pesan: String) {
    // PENTING: prioritaskan membuka APLIKASI WhatsApp yang terinstall di HP
    // (bukan WhatsApp Web/browser). Coba WhatsApp reguler dulu, lalu WhatsApp
    // Business, baru fallback ke wa.me kalau memang tidak ada satupun yang
    // terinstall (supaya tetap ada jalan keluar, bukan macet total).
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$WA_ADMIN_NUMBER&text=" + Uri.encode(pesan))
    val paketWaCoba = listOf("com.whatsapp", "com.whatsapp.w4b")
    for (pkg in paketWaCoba) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).setPackage(pkg)
            context.startActivity(intent)
            return
        } catch (e: Exception) { /* app itu tidak terinstall, coba paket berikutnya */ }
    }
    // Tidak ada WhatsApp terinstall sama sekali - fallback terakhir.
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$WA_ADMIN_NUMBER?text=" + Uri.encode(pesan))))
}

@Composable
private fun JadwalShalatCard() {
    val context = LocalContext.current
    var timings by remember { mutableStateOf<TimingsData?>(null) }
    var namaLokasi by remember { mutableStateOf("Makkah") }
    var loading by remember { mutableStateOf(true) }

    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* hasil ditangani lewat pengecekan ulang di LaunchedEffect berikutnya */ }

    LaunchedEffect(Unit) {
        var lat = MAKKAH_LAT
        var lng = MAKKAH_LNG

        val sudahDiizinkan = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!sudahDiizinkan) {
            permLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val fused = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                val loc = fused.lastLocation.await()
                if (loc != null) {
                    lat = loc.latitude
                    lng = loc.longitude
                    // Reverse geocode - kalau gagal (mis. tidak ada koneksi/layanan geocoding),
                    // tetap lanjut pakai koordinatnya, cuma label kota fallback ke "Makkah".
                    try {
                        val geocoder = android.location.Geocoder(context, java.util.Locale("in", "ID"))
                        @Suppress("DEPRECATION")
                        val hasil = geocoder.getFromLocation(lat, lng, 1)
                        val kota = hasil?.firstOrNull()?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
                        if (!kota.isNullOrBlank()) namaLokasi = kota
                    } catch (e: Exception) { /* biarkan label default */ }
                }
            }
        } catch (e: Exception) {
            // gagal ambil lokasi (izin ditolak/GPS mati) - tetap lanjut pakai fallback Makkah
        }

        try {
            val res = AladhanApi.service.getTimings(lat = lat, lng = lng)
            timings = res.data?.timings
        } catch (e: Exception) {
            // diam-diam gagal - kartu ini opsional, tidak boleh menghalangi layar utama
        } finally { loading = false }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(BrandGreen, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            "🌙  Jadwal Shalat — $namaLokasi", color = BrandGoldSoft, fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp, fontFamily = FontFamily.Serif
        )
        Text("Menyesuaikan lokasi HP kamu saat ini", color = Sand.copy(alpha = 0.7f), fontSize = 10.5.sp, modifier = Modifier.padding(bottom = 10.dp))
        when {
            loading -> Text("Memuat...", color = Sand, fontSize = 12.sp)
            timings == null -> Text("Jadwal tidak tersedia - periksa koneksi.", color = Sand, fontSize = 12.sp)
            else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WaktuShalatItem("Subuh", timings!!.Fajr)
                WaktuShalatItem("Terbit", timings!!.Sunrise)
                WaktuShalatItem("Dzuhur", timings!!.Dhuhr)
                WaktuShalatItem("Ashar", timings!!.Asr)
                WaktuShalatItem("Maghrib", timings!!.Maghrib)
                WaktuShalatItem("Isya", timings!!.Isha)
            }
        }
    }
}

@Composable
private fun WaktuShalatItem(label: String, waktu: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = BrandGoldSoft, fontSize = 9.5.sp)
        Spacer(Modifier.height(2.dp))
        Text(waktu?.substringBefore(" ") ?: "-", color = Sand, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PaketCard(paket: Paket, jumlahJadwal: Int, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(16.dp))
            .border(1.dp, if (!paket.badge.isNullOrBlank()) BrandGold.copy(alpha = 0.5f) else Color(0xFFE8E2D4), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).background(BrandGoldSoft.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(paket.icon ?: "\uD83D\uDCE6", fontSize = 17.sp) }
            Spacer(Modifier.width(12.dp))
            Text(
                paket.nama ?: "-", color = TextDarkGreen, fontWeight = FontWeight.Bold, fontSize = 15.5.sp,
                modifier = Modifier.weight(1f)
            )
            if (!paket.badge.isNullOrBlank()) {
                Box(Modifier.background(BrandGold, RoundedCornerShape(6.dp)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                    Text(paket.badge, color = TextDarkGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(paket.harga ?: "", color = BrandGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (jumlahJadwal > 0) {
                Text("🕐", fontSize = 10.sp)
                Spacer(Modifier.width(3.dp))
                Text("$jumlahJadwal jadwal tersedia", color = TextMutedOnCream, fontSize = 10.5.sp)
            }
        }
    }
}

/**
 * Detail paket NATIF (bukan WebView, bukan browser). Menampilkan info ringkas
 * paket + daftar jadwal keberangkatan asli, plus tombol "Daftar Paket Ini"
 * yang membuka WhatsApp admin dengan pesan yang sudah menyebut nama paketnya.
 */
@Composable
private fun PaketDetailScreen(paket: Paket, trips: List<TripLive>, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(CreamBg).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            TextButton(onClick = onBack) {
                Text("< Kembali", color = BrandGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(Modifier.fillMaxWidth().background(BrandGreen, RoundedCornerShape(18.dp)).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(paket.icon ?: "\uD83D\uDCE6", fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(paket.nama ?: "-", color = Sand, fontWeight = FontWeight.Bold, fontSize = 19.sp, fontFamily = FontFamily.Serif)
                    paket.tipe?.let { Text(it, color = Sand.copy(alpha = 0.7f), fontSize = 11.sp) }
                }
            }
            if (!paket.badge.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.background(BrandGold, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(paket.badge, color = TextDarkGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(paket.subtitle ?: "", color = Sand, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Divider(color = Sand.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            Text("Harga mulai dari", color = Sand.copy(alpha = 0.7f), fontSize = 11.sp)
            Text(paket.harga ?: "-", color = BrandGoldSoft, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))
        WhatsappCta("Daftar Paket Ini via WhatsApp") {
            val pesan = "Assalamu'alaikum, saya ingin daftar paket ${paket.nama} (${paket.harga})."
            bukaWhatsappAdmin(context, pesan)
        }

        Spacer(Modifier.height(18.dp))
        Text("Jadwal Keberangkatan", color = TextDarkGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        if (trips.isEmpty()) {
            Text(
                "Belum ada jadwal live yang bisa ditarik. Pastikan plugin WordPress imtiyaz-connector sudah aktif, atau memang belum ada keberangkatan terbuka untuk tipe ini.",
                color = TextMutedOnCream, fontSize = 12.sp
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trips) { trip ->
                    TripRow(trip) {
                        val pesan = "Assalamu'alaikum, saya ingin daftar keberangkatan ${trip.judul} (${trip.tanggal})."
                        bukaWhatsappAdmin(context, pesan)
                    }
                }
            }
        }
    }
}

@Composable
private fun TripRow(trip: TripLive, onDaftarClick: () -> Unit) {
    val isClosed = trip.status == "CLOSED"
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE8E2D4), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(trip.judul ?: "-", color = TextDarkGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box(
                Modifier.background(if (isClosed) Danger else SafeColor, RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(if (isClosed) "CLOSED" else "OPEN", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Keberangkatan: ${trip.tanggal ?: "-"}", color = TextMutedOnCream, fontSize = 11.sp)
        trip.durasi?.let {
            Spacer(Modifier.height(2.dp))
            Text("Durasi: $it", color = TextMutedOnCream, fontSize = 11.sp)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(trip.harga ?: "-", color = BrandGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (!isClosed) {
                Box(
                    Modifier
                        .background(Color(0xFF25D366), RoundedCornerShape(8.dp))
                        .clickable(onClick = onDaftarClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("💬 Daftar via WA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
