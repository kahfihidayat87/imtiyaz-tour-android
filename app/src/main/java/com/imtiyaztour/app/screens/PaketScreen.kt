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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*
import com.imtiyaztour.app.R
import kotlinx.coroutines.tasks.await

// Nomor WhatsApp admin resmi IMTIYAZ (0811-277-6543) dalam format internasional.
private const val WA_ADMIN_NUMBER = "628112776543"

// PENTING: layar ini TIDAK membuka WebView atau browser eksternal.
// Satu-satunya intent ke aplikasi lain di sini adalah membuka WhatsApp lewat
// tombol Chat/Daftar - permintaan eksplisit, bukan menampilkan konten web di app.

@Composable
fun PaketScreen(namaJamaah: String, onLogout: () -> Unit, onOpenAkun: () -> Unit = {}) {
    val context = LocalContext.current
    var trips by remember { mutableStateOf<List<TripLive>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            trips = ImtiyazApi.service.getPaketLive().latest_trips
        } catch (e: Exception) {
            errorMsg = "Gagal memuat jadwal - periksa koneksi. (${e.message})"
        } finally {
            loading = false
        }
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
                        "JADWAL UMRAH", color = TextDarkGreen, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(28.dp).height(1.dp).background(BrandGold))
                        Text(" ◈ ", color = BrandGold, fontSize = 11.sp)
                        Box(Modifier.width(28.dp).height(1.dp).background(BrandGold))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Semua paket, diperbarui otomatis dari sistem", color = TextMutedOnCream,
                        fontSize = 10.5.sp
                    )
                }
            }
            when {
                loading -> item {
                    Box(Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandGreen)
                    }
                }
                errorMsg != null -> item { Text(errorMsg!!, color = Danger, fontSize = 13.sp) }
                trips.isEmpty() -> item {
                    Text(
                        "Belum ada jadwal keberangkatan baru yang akan datang. Hubungi kami untuk info terbaru.",
                        color = TextMutedOnCream, fontSize = 12.5.sp
                    )
                }
                else -> items(trips) { trip ->
                    JadwalCard(trip) {
                        val pesan = "Assalamu'alaikum, saya ingin daftar keberangkatan ${trip.judul} (${trip.tanggal})."
                        bukaWhatsappAdmin(context, pesan)
                    }
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
            Box(Modifier.size(24.dp).background(Color(0xFF25D366), CircleShape), contentAlignment = Alignment.Center) {
                Text("📞", fontSize = 11.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(label.removePrefix("💬 "), color = TextDarkGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

private fun bukaWhatsappAdmin(context: android.content.Context, pesan: String) {
    // Prioritaskan APLIKASI WhatsApp yang terinstall (bukan WhatsApp Web/browser).
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$WA_ADMIN_NUMBER&text=" + Uri.encode(pesan))
    for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage(pkg))
            return
        } catch (e: Exception) { }
    }
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
    ) { }

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
                    try {
                        val geocoder = android.location.Geocoder(context, java.util.Locale("in", "ID"))
                        @Suppress("DEPRECATION")
                        val hasil = geocoder.getFromLocation(lat, lng, 1)
                        val kota = hasil?.firstOrNull()?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
                        if (!kota.isNullOrBlank()) namaLokasi = kota
                    } catch (e: Exception) { }
                }
            }
        } catch (e: Exception) { }

        try {
            val res = AladhanApi.service.getTimings(lat = lat, lng = lng)
            timings = res.data?.timings
        } catch (e: Exception) {
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

/** Kartu satu baris jadwal keberangkatan (semua paket digabung), gaya krem konsisten dengan Beranda. */
@Composable
private fun JadwalCard(trip: TripLive, onDaftarClick: () -> Unit) {
    val isClosed = trip.status == "CLOSED"
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE8E2D4), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(trip.tanggal ?: "-", color = BrandGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(trip.judul ?: trip.tipe ?: "-", color = TextDarkGreen, fontSize = 12.sp)
                trip.durasi?.let {
                    Spacer(Modifier.height(2.dp))
                    Text("Durasi: $it", color = TextMutedOnCream, fontSize = 10.5.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                trip.harga_asli?.let {
                    Text(it, color = TextMutedOnCream, fontSize = 10.5.sp, textDecoration = TextDecoration.LineThrough)
                }
                Text(trip.harga ?: "-", color = BrandGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (!isClosed) {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF25D366), RoundedCornerShape(9.dp))
                    .clickable(onClick = onDaftarClick)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("💬 Daftar Jadwal Ini via WA", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
