package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

// PENTING: layar ini TIDAK membuka WebView atau browser eksternal sama sekali.
// Tap kartu paket -> pindah ke detail NATIF di dalam app (Compose biasa),
// menampilkan jadwal keberangkatan asli dari GET /api/paket-live (yang
// menarik data trip live dari WP Travel Engine lewat plugin imtiyaz-connector).

@Composable
fun PaketScreen() {
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
            // fallback ke /api/paket kalau /api/paket-live belum tersedia (mis. plugin WP belum aktif)
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

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Paket Umrah", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Diambil langsung dari pastiumrah.com", color = Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

        when {
            loading -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGold)
            }
            errorMsg != null -> Text(errorMsg!!, color = Danger, fontSize = 13.sp)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(paketList) { paket ->
                    val jumlahJadwal = allTrips.count { it.tipe == paket.tipe || it.tipe_slug == paket.id }
                    PaketCard(paket, jumlahJadwal) { selectedPaket = paket }
                }
            }
        }
    }
}

@Composable
private fun PaketCard(paket: Paket, jumlahJadwal: Int, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(paket.icon ?: "\uD83D\uDCE6", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(paket.nama ?: "-", color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                if (!paket.badge.isNullOrBlank()) {
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier.background(BrandGold, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(paket.badge, color = BrandGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(paket.subtitle ?: "", color = Muted, fontSize = 12.5.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(paket.harga ?: "", color = BrandGoldSoft, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (jumlahJadwal > 0) {
                    Spacer(Modifier.weight(1f))
                    Text("$jumlahJadwal jadwal tersedia", color = Muted, fontSize = 10.5.sp)
                }
            }
        }
    }
}

/**
 * Detail paket NATIF (bukan WebView, bukan browser). Menampilkan info ringkas
 * paket + daftar jadwal keberangkatan asli (tanggal, harga per jadwal, status
 * OPEN/CLOSED) hasil tarikan live dari WP Travel Engine lewat plugin
 * imtiyaz-connector. Kalau daftar jadwal kosong, kemungkinan plugin WP belum
 * aktif atau memang belum ada jadwal terbuka untuk tipe ini.
 */
@Composable
private fun PaketDetailScreen(paket: Paket, trips: List<TripLive>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            TextButton(onClick = onBack) {
                Text("< Kembali", color = BrandGoldSoft, fontSize = 13.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(paket.icon ?: "\uD83D\uDCE6", fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(paket.nama ?: "-", color = Sand, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        paket.tipe?.let { Text(it, color = Muted, fontSize = 11.sp) }
                    }
                }
                if (!paket.badge.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.background(BrandGold, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(paket.badge, color = BrandGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(paket.subtitle ?: "", color = Sand, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Divider(color = LineColor)
                Spacer(Modifier.height(16.dp))
                Text("Harga mulai dari", color = Muted, fontSize = 11.sp)
                Text(paket.harga ?: "-", color = BrandGoldSoft, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Jadwal Keberangkatan", color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        if (trips.isEmpty()) {
            Text(
                "Belum ada jadwal live yang bisa ditarik. Pastikan plugin WordPress imtiyaz-connector sudah aktif, atau memang belum ada keberangkatan terbuka untuk tipe ini.",
                color = Muted, fontSize = 12.sp
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trips) { trip -> TripRow(trip) }
            }
        }
    }
}

@Composable
private fun TripRow(trip: TripLive) {
    val isClosed = trip.status == "CLOSED"
    Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(trip.judul ?: "-", color = Sand, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(
                    Modifier.background(if (isClosed) Danger else SafeColor, RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(if (isClosed) "CLOSED" else "OPEN", color = Sand, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Keberangkatan: ${trip.tanggal ?: "-"}", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(trip.harga ?: "-", color = BrandGoldSoft, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
