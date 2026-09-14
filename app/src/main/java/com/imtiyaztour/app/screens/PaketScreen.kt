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
// hanya memakai data yang sudah didapat dari GET /api/paket. Tidak ada
// Intent.ACTION_VIEW / WebView ke pastiumrah.com di layar ini.

@Composable
fun PaketScreen() {
    var paketList by remember { mutableStateOf<List<Paket>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedPaket by remember { mutableStateOf<Paket?>(null) }

    LaunchedEffect(Unit) {
        try {
            paketList = ImtiyazApi.service.getPaket()
        } catch (e: Exception) {
            errorMsg = "Gagal memuat paket - periksa koneksi. (${e.message})"
        } finally {
            loading = false
        }
    }

    val current = selectedPaket
    if (current != null) {
        PaketDetailScreen(paket = current, onBack = { selectedPaket = null })
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
                    PaketCard(paket) { selectedPaket = paket }
                }
            }
        }
    }
}

@Composable
private fun PaketCard(paket: Paket, onClick: () -> Unit) {
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
            Text(paket.harga ?: "", color = BrandGoldSoft, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Detail paket NATIF (bukan WebView, bukan browser). Hanya menampilkan ulang
 * data dari objek Paket yang sudah ada di memori (hasil GET /api/paket).
 * Kalau nanti admin mau menambah field detail lain (fasilitas, itinerary, dll),
 * field itu perlu ditambahkan di backend `PAKET_EXISTING` / respons WP, lalu
 * ditampilkan di sini - bukan dengan membuka halaman webnya.
 */
@Composable
private fun PaketDetailScreen(paket: Paket, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
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
                Text("Harga", color = Muted, fontSize = 11.sp)
                Text(paket.harga ?: "-", color = BrandGoldSoft, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "Detail lengkap (jadwal keberangkatan, itinerary, fasilitas) belum tersedia dari API saat ini. Info ini hanya menampilkan data ringkas yang dikirim backend, tanpa membuka halaman web.",
            color = Muted, fontSize = 11.5.sp
        )
    }
}
